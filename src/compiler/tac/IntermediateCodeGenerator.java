package compiler.tac;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.*;
import compiler.parser.ast.nodes.declarations.*;
import compiler.parser.ast.nodes.expressions.*;
import compiler.parser.ast.nodes.expressions.operations.*;
import compiler.parser.ast.nodes.statements.*;
import compiler.parser.ast.nodes.structures.ProgramNode;
import compiler.parser.ast.nodes.terminals.*;
import compiler.typechecker.TypeChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class IntermediateCodeGenerator implements ASTVisitor {
    public ProgramNode program = null;
    public List<StatementNode> currentStatements;
    public Stack<LabelNode> loopEndLabels = new Stack<>();

    public IntermediateCodeGenerator(TypeChecker typeChecker) {
        typeChecker.program.accept(this);
        program = typeChecker.program;
    }

    @Override
    public void visit(ProgramNode node) {
        currentStatements = new ArrayList<>();
        node.block.accept(this);
    }

    @Override
    public void visit(BlockNode node) {
        for (StatementNode statement : node.statements)
            statement.accept(this);
    }

    @Override
    public void visit(AssignmentNode node) {
        node.left = reduceLocNode(node.left, false);
        node.expression = reduceExpression(node.expression, node.left.isArray());
        emitAssignment(node.left, node.expression);
    }

    @Override
    public void visit(IfNode node) {
        node.expression = reduceExpression(node.expression, false);

        LabelNode falseLabel = LabelNode.newLabel();
        // End label only necessary if there is an else statement.
        LabelNode endLabel = (node.elseStatement != null) ? LabelNode.newLabel() : null;

        emitIfFalse(node.expression, falseLabel);

        node.thenStatement.accept(this);

        if (node.elseStatement != null)
            emitGoto(endLabel);

        emitLabel(falseLabel);

        if (node.elseStatement != null) {
            node.elseStatement.accept(this);
            emitLabel(endLabel);
        }
    }

    @Override
    public void visit(WhileNode node) {
        LabelNode startLabel = LabelNode.newLabel();
        LabelNode endLabel = LabelNode.newLabel();
        emitLabel(startLabel);

        // If it is a true literal, we don't need a false check.
        if (!(node.expression instanceof TrueNode)) {
            node.expression = reduceExpression(node.expression, false);
            emitIfFalse(node.expression, endLabel);
        }

        loopEndLabels.push(endLabel);
        node.body.accept(this);
        loopEndLabels.pop();

        emitGoto(startLabel);
        emitLabel(endLabel);
    }

    @Override
    public void visit(DoWhileNode node) {
        LabelNode startLabel =LabelNode.newLabel();
        LabelNode endLabel = LabelNode.newLabel();
        emitLabel(startLabel);

        loopEndLabels.push(endLabel);
        node.body.accept(this);
        loopEndLabels.pop();

        node.expression = reduceExpression(node.expression, false);

        emitIfTrue(node.expression, startLabel);
    }


    @Override
    public void visit(BreakNode node) {
        emitGoto(loopEndLabels.peek());
    }

    public ExpressionNode reduceExpression(ExpressionNode n, Boolean needSingleResult) {
        if (n instanceof BinaryExpressionNode)
            return reduceBinaryExpression((BinaryExpressionNode) n, needSingleResult);
        if (n instanceof UnaryNode)
            return reduceUnaryExpression((UnaryNode) n, needSingleResult);
        if (n instanceof LocNode)
            return reduceLocNode((LocNode) n, needSingleResult);
        if (n instanceof ParenthesisNode)
            return reduceExpression(((ParenthesisNode) n).expression, needSingleResult);
        return n;
    }

    public ExpressionNode reduceBinaryExpression(BinaryExpressionNode n, Boolean needSingleResult) {
        ExpressionNode left = reduceExpression(n.left, true);
        ExpressionNode right = reduceExpression(n.right, true);

        // If it needs to be a single result, store the binary expression as a temporary variable.
        BinaryExpressionNode result = new BinaryExpressionNode(left, right, n.operator);
        if (needSingleResult) {
            TempNode temp = TempNode.newTemp();
            emitAssignment(temp, result);
            return temp;
        }
        return result;
    }

    public ExpressionNode reduceUnaryExpression(UnaryNode n, Boolean needSingleResult) {
        n.expression = reduceExpression(n.expression, needSingleResult);
        return n;
    }

    public LocNode reduceLocNode(LocNode n, Boolean needSingleResult) {
        // If it's not an array, just return the loc node which is a single identifier.
        if (!n.isArray())
            return n;

        // For each dimensional accessor, reduce the expression and store as a list.
        // (e.g. a[1][1+2][3] -> [1, t1, 3])
        List<ExpressionNode> reducedDimensions = reduceDimensions(n);

        // Go through each dimension and calculate the accumulated offset.
        ExpressionNode totalOffset = null;
        for (int i = 0; i < reducedDimensions.size(); i++) {
            // Calculate the offset of the dimension based on the stride.
            ExpressionNode additionalOffset = calculateOffset(n, i, reducedDimensions);
            // Add this additional offset to the total offset.
            totalOffset = addAdditionalOffset(totalOffset, additionalOffset);
        }

        // Create a final loc node that uses the initial identifier and the final offset value to access it.
        LocNode finalLoc = createFinalLoc(n, totalOffset);

        // If it requires a single result, store the array access as a temporary variable.
        // (e.g. a[100] = a[10] -> t1 = a[10]; a[100] = t1)
        if (needSingleResult) {
            TempNode temp = TempNode.newTemp();
            emitAssignment(temp, finalLoc);
            return temp;
        } else {
            return finalLoc;
        }
}

    private LocNode createFinalLoc(LocNode n, ExpressionNode totalOffset) {
        TempNode finalOffset = TempNode.newTemp();
        // Multiply the total offset by the width (bytes) of the type to get the final offset value.
        emitAssignment(finalOffset, new BinaryExpressionNode(totalOffset, n.getWidthNumNode(), "*"));
        // Create a new loc node with the final offset, n[finalOffset].
        return new LocNode(n.id, new ArrayLocNode(null, finalOffset));
    }

    private ExpressionNode addAdditionalOffset(ExpressionNode totalOffset, ExpressionNode additionalOffset) {
        // If it's the first offset, initialize the total offset with this additional offset.
        if (totalOffset == null) {
            totalOffset = additionalOffset;
        // Otherwise, add the additional offset to the total offset.
        } else {
            TempNode temp = TempNode.newTemp();
            emitAssignment(temp, new BinaryExpressionNode(totalOffset, additionalOffset, "+"));
            totalOffset = temp;
        }
        return totalOffset;
    }

    private ExpressionNode calculateOffset(LocNode n, int i, List<ExpressionNode> reducedDimensions) {
        // Get the dimension expression.
        // (e.g. a[10][20] would return 10 for i = 0 and 20 for i = 1)
        ExpressionNode dimension = reducedDimensions.get(i);
        ExpressionNode additionalOffset;
        // If it's the last dimension, you don't need to calculate the stride because it's 1.
        if (i == reducedDimensions.size() - 1) {
            additionalOffset = dimension;
        // Otherwise, calculate stride and multiply it by the dimension expression.
        } else {
            TempNode temp = TempNode.newTemp();
            NumNode stride = new NumNode(calculateStride(n.id.getType(), i));
            emitAssignment(temp, new BinaryExpressionNode(dimension, stride, "*"));
            additionalOffset = temp;
        }
        return additionalOffset;
    }

    private static int calculateStride(TypeNode type, int dimension) {
        int stride = 1;
        // Excluding the first dimension, find the product of the declared sizes of the dimensions.
        for (int i = dimension + 1; i < type.getDepth(); i++)
            stride *= type.getDimensionSize(i).num;
        return stride;
    }

    private List<ExpressionNode> reduceDimensions(LocNode n) {
        List<ExpressionNode> reducedDimensions = new ArrayList<>();
        // Traverse through each dimension and reduce the expression, and reduce the expression.
        for (ArrayLocNode a = n.array; a != null; a = a.array)
            reducedDimensions.add(reduceExpression(a.expression, true));
        return reducedDimensions;
    }

    public void emitAssignment(LocNode left, ExpressionNode right) {
        AssignmentNode assign = new AssignmentNode();
        assign.left = left;
        assign.expression = right;
        currentStatements.add(assign);
    }

    public void emitLabel(LabelNode label) {
        currentStatements.add(label);
    }

    public void emitGoto(LabelNode label) {
        currentStatements.add(new GotoNode(label));
    }

    public void emitIfFalse(ExpressionNode expression, LabelNode label) {
        emitLabel(LabelNode.newLabel());
        currentStatements.add(new IfFalseNode(expression));
        currentStatements.add(new GotoNode(label));
    }

    public void emitIfTrue(ExpressionNode expression, LabelNode label) {
        currentStatements.add(new IfTrueNode(expression));
        currentStatements.add(new GotoNode(label));
    }
}
