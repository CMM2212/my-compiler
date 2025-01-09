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
    int statementIndex = 0;

    boolean lhsIsArray = false; //  If LHS is array, right must be unary.
    boolean rhsIsArray = false; //  Tracking to mimic book output
    boolean isTopBinaryExpression = false;

    Stack<LabelNode> loopEndLabels = new Stack<>();
    LabelNode previousLabel = null;

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
        int previousIndex = statementIndex;
        boolean previousIsArray = lhsIsArray;
        boolean previousRhsIsArray = rhsIsArray;
        boolean previousIsTopBinaryExpression = isTopBinaryExpression;
        LabelNode previousPreviousLabel = previousLabel;
        previousLabel = null;

        for (StatementNode statement : n.statements) {
            statement.accept(this);
            currentStatements.add(statement);
        }

        previousLabel = previousPreviousLabel;
        n.statements = currentStatements;
        statementIndex = previousIndex;
        lhsIsArray = previousIsArray;
        rhsIsArray = previousRhsIsArray;
        isTopBinaryExpression = previousIsTopBinaryExpression;
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
        LabelNode startLabel = previousLabel != null ? previousLabel : LabelNode.newLabel();
        LabelNode endLabel = LabelNode.newLabel();
        previousLabel = endLabel;
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
        LabelNode startLabel = previousLabel != null ? previousLabel : LabelNode.newLabel();
        LabelNode endLabel = LabelNode.newLabel();
        previousLabel = endLabel;
        loopEndLabels.push(endLabel);
        emitLabel(startLabel);
        n.body.accept(this);
        n.expression = reduceExpression(n.expression, true);

        emitIfTrue(n.expression, startLabel);
//        emitLabel(endLabel);
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
    if (!n.array.isArray()) {
        n.array.expression = reduceExpression(n.array.expression, true);
    }
    if (n.array.array == null) {

        n.array.expression = reduceExpression(n.array.expression, true);

        int size = n.getWidth();
        NumNode sizeNode = new NumNode(size);
        TempNode byteOffset = TempNode.newTemp();
        emitAssignment(byteOffset, new BinaryExpressionNode(n.array.expression, sizeNode, "*"));
        n.array.expression = byteOffset;
        return n;
    }

    NumNode width = new NumNode(n.getWidth());
    TypeNode type = n.id.getType();
    int depth = type.getDepth();

    LocNode prevLoc = null;

    int index = 0;
    for (ArrayLocNode a = n.array; a != null; a = a.array) {
        index++;
        a.expression = reduceExpression(a.expression, true);

        if (index == depth) {
            if (prevLoc != null) {
                TempNode combinedOffset = TempNode.newTemp();
                emitAssignment(combinedOffset,
                               new BinaryExpressionNode(prevLoc, a.expression, "+"));
                prevLoc = combinedOffset;
            } else {
                prevLoc = (LocNode) a.expression;
            }
        } else {
            int stride = 1;
            for (int i = index; i < depth; i++) {
                stride *= type.getDimSize(i).num;
            }

            TempNode currentOffset = TempNode.newTemp();
            emitAssignment(currentOffset,
                           new BinaryExpressionNode(a.expression, new NumNode(stride), "*"));

            if (prevLoc != null) {
                TempNode combinedOffset = TempNode.newTemp();
                emitAssignment(combinedOffset,
                               new BinaryExpressionNode(prevLoc, currentOffset, "+"));
                prevLoc = combinedOffset;
            } else {
                prevLoc = currentOffset;
            }
        }
    }

    TempNode byteOffset = TempNode.newTemp();
    emitAssignment(byteOffset, new BinaryExpressionNode(prevLoc, width, "*"));

    n.array = new ArrayLocNode(null, byteOffset);
    return n;
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
