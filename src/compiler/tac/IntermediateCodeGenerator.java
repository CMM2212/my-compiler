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


    // Main Nodes
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ProgramNode n) {
        currentStatements = new ArrayList<>();
        n.block.accept(this);
    }

    @Override
    public void visit(BlockNode n) {

        for (StatementNode statement : n.statements) {
            statement.accept(this);
            currentStatements.add(statement);
        }
    }

    // Statement Nodes
    ///////////////////////////////////////////////////////////////////////////////


    @Override
    public void visit(AssignmentNode n) {
        n.left = reduceLocNode(n.left, false);
        n.expression = reduceExpression(n.expression, n.left.isArray());
    }

    @Override
    public void visit(IfNode n) {
        n.expression = reduceExpression(n.expression, true);

        LabelNode falseLabel = LabelNode.newLabel();
        LabelNode endLabel = (n.elseStatement != null) ? LabelNode.newLabel() : null;

        emitIfFalse(n.expression, falseLabel);

        if (n.elseStatement != null)
            LabelNode.newLabel();

        n.thenStatement.accept(this);

        if (n.elseStatement != null) {
            emitGoto(endLabel);
        }

        emitLabel(falseLabel);

        if (n.elseStatement != null) {
            n.elseStatement.accept(this);
            emitLabel(endLabel);
        }
    }

    @Override
    public void visit(WhileNode n) {
        LabelNode startLabel = LabelNode.newLabel();
        LabelNode endLabel = LabelNode.newLabel();
        loopEndLabels.push(endLabel);
        emitLabel(startLabel);

        if (!(n.expression instanceof TrueNode)) {
            n.expression = reduceExpression(n.expression, true);
            emitIfFalse(n.expression, endLabel);
        }

        n.body.accept(this);
        emitGoto(startLabel);
        emitLabel(endLabel);
        loopEndLabels.pop();
    }

    @Override
    public void visit(DoWhileNode n) {
        LabelNode startLabel =LabelNode.newLabel();
        LabelNode endLabel = LabelNode.newLabel();
        loopEndLabels.push(endLabel);
        emitLabel(startLabel);
        n.body.accept(this);
        n.expression = reduceExpression(n.expression, true);

        emitIfTrue(n.expression, startLabel);
        loopEndLabels.pop();
    }

    @Override
    public void visit(LocNode n) {
        n.id.accept(this);
        if (n.array != null) {
            n.array.accept(this);
        }
    }

    @Override
    public void visit(ArrayLocNode n) {
        n.expression.accept(this);
        if (n.array != null) {
            n.array.accept(this);
        }
    }


    // Terminal Nodes
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ParenthesisNode n) {
        n.expression.accept(this);
    }

    @Override
    public void visit(BreakNode n) {
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

        if (needSingleResult) {
            TempNode temp = TempNode.newTemp();
            emitAssignment(temp, new BinaryExpressionNode(left, right, n.operator));
            return temp;
        }
        return new BinaryExpressionNode(left, right, n.operator);
    }

    public ExpressionNode reduceUnaryExpression(UnaryNode n, Boolean needSingleResult) {
        return reduceExpression(n.right, needSingleResult);
    }

    public LocNode reduceLocNode(LocNode n, Boolean needSingleResult) {
        if (!n.isArray())
            return n;

        List<ExpressionNode> reducedDimensions = reduceDimensions(n);
        ExpressionNode totalOffset = null;

        for (int i = 0; i < reducedDimensions.size(); i++) {
            ExpressionNode additionalOffset;
            additionalOffset = calculateOffset(n, i, reducedDimensions);
            totalOffset = addAdditionalOffset(totalOffset, additionalOffset);
        }

        LocNode finalLoc = createFinalLoc(n, totalOffset);

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
        emitAssignment(finalOffset, new BinaryExpressionNode(totalOffset, n.getWidthNumNode(), "*"));
        return new LocNode(n.id, new ArrayLocNode(null, finalOffset));
    }

    private ExpressionNode addAdditionalOffset(ExpressionNode totalOffset, ExpressionNode additionalOffset) {
        if (totalOffset == null) {
            totalOffset = additionalOffset;
        } else {
            TempNode temp = TempNode.newTemp();
            emitAssignment(temp, new BinaryExpressionNode(totalOffset, additionalOffset, "+"));
            totalOffset = temp;
        }
        return totalOffset;
    }

    private ExpressionNode calculateOffset(LocNode n, int i, List<ExpressionNode> reducedDimensions) {
        ExpressionNode dimension = reducedDimensions.get(i);
        ExpressionNode additionalOffset;
        if (i == reducedDimensions.size() - 1) {
            additionalOffset = dimension;
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
        for (int i = dimension + 1; i < type.getDepth(); i++)
            stride *= type.getDimSize(i).num;
        return stride;
    }

    private List<ExpressionNode> reduceDimensions(LocNode n) {
        List<ExpressionNode> reducedDimensions = new ArrayList<>();
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
