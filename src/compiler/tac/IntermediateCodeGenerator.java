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
    BlockNode currentBlock;
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

//    @Override
//    public void visit(DeclNode n) {
//        n.type.accept(this);
//        n.id.accept(this);
//    }
//
//    @Override
//    public void visit(TypeNode n) {
//        n.type.accept(this);
//        if (n.array != null)
//            n.array.accept(this);
//    }
//
//    @Override
//    public void visit(ArrayTypeNode n) {
//        n.size.accept(this);
//        if (n.type != null)
//            n.type.accept(this);
//    }

    // Statement Nodes
    ///////////////////////////////////////////////////////////////////////////////


    @Override
    public void visit(AssignmentNode n) {
        n.left = (LocNode) reduceExpression(n.left);
        lhsIsArray = n.left.array != null;
        isTopBinaryExpression = true;
//        if (lhsIsArray) {
//
//            LabelNode randomLabel = LabelNode.newLabel();
//            currentStmts.add(currentStmts.toArray().length - 1, new StmtNode(randomLabel));
//        }
        n.expression = reduceExpression(n.expression);

    }

    @Override
    public void visit(IfNode n) {
        n.expression = reduceExpression(n.expression);

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
            n.expression = reduceExpression(n.expression);
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
        n.expression = reduceExpression(n.expression);

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

    // Expression Nodes
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(BinaryExpressionNode n) {
        n.left = reduceExpression(n.left);
        n.right = reduceExpression(n.right);
    }

    @Override
    public void visit(UnaryNode n) {
        n.right = reduceExpression(n.right);
    }

    // Terminal Nodes
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ParenthesisNode n) {
        n.expression.accept(this);
    }

    @Override
    public void visit(BreakNode n) {

//        LabelNode someLabel = LabelNode.newLabel();
//        emitLabel(someLabel);
        emitGoto(loopEndLabels.peek());
    }

    public ExpressionNode reduceExpression(ExpressionNode n) {
        if (n instanceof BinaryExpressionNode)
            return reduceBinaryExpression((BinaryExpressionNode) n);
        if (n instanceof UnaryNode)
            return reduceUnaryExpression((UnaryNode) n);
        if (n instanceof LocNode)
            return reduceLocNode((LocNode) n);
        if (n instanceof ParenthesisNode)
            return reduceExpression(((ParenthesisNode) n).expression);
        return n;
    }

    public ExpressionNode reduceBinaryExpression(BinaryExpressionNode n) {
        boolean prevIsTopBinaryExpression = isTopBinaryExpression;
        isTopBinaryExpression = false;
        boolean hasArray = false;
        ExpressionNode left = reduceExpression(n.left);
        ExpressionNode right = reduceExpression(n.right);

        if (right instanceof LocNode) {
            LocNode l = (LocNode) right;
            if (l.array != null) {
                TempNode temp = TempNode.newTemp();
                LocNode tempLoc = new LocNode(temp);
                emitAssignment(tempLoc, right);
                right = tempLoc;
                hasArray = true;
                rhsIsArray = true;
            }
        }

        if (left instanceof LocNode) {
            LocNode l = (LocNode) left;
            if (l.array != null) {
                TempNode temp = TempNode.newTemp();
                LocNode tempLoc = new LocNode(temp);
                emitAssignment(tempLoc, left);
                left = tempLoc;
                hasArray = true;
                rhsIsArray = true;
            }
        }


        if (prevIsTopBinaryExpression && !lhsIsArray && hasArray) {
            n.left = left;
            n.right = right;
            return n;
        }


            n.left = left;
            n.right = right;
            return n;

//        TempNode temp = TempNode.newTemp();
//        LocNode loc = new LocNode(temp);
//
//        emitAssignment(loc, new BinaryExpressionNode(left, right, n.operator));
//
//        return loc;
    }

    public ExpressionNode reduceUnaryExpression(UnaryNode n) {
        ExpressionNode right = reduceExpression(n.right);
        if (n.op == null && !lhsIsArray) {
            return right;
        }

        TempNode temp = TempNode.newTemp();
        LocNode loc = new LocNode(temp);

        emitAssignment(loc, new UnaryNode(n.op, right));
        return loc;
    }

public ExpressionNode reduceLocNode(LocNode n) {
    if (n.array == null)
        return n;
    if (n.array.array == null) {

        n.array.expression = reduceExpression(n.array.expression);

        int size = n.getWidth();
        NumNode sizeNode = new NumNode(size);
        TempNode temp = TempNode.newTemp();
        LocNode byteOffset = new LocNode(temp);
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
        a.expression = reduceExpression(a.expression);

        if (index == depth) {
            if (prevLoc != null) {
                TempNode temp = TempNode.newTemp();
                LocNode combinedOffset = new LocNode(temp);
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

            TempNode temp = TempNode.newTemp();
            LocNode currentOffset = new LocNode(temp);
            emitAssignment(currentOffset,
                           new BinaryExpressionNode(a.expression, new NumNode(stride), "*"));

            if (prevLoc != null) {
                temp = TempNode.newTemp();
                LocNode combinedOffset = new LocNode(temp);
                emitAssignment(combinedOffset,
                               new BinaryExpressionNode(prevLoc, currentOffset, "+"));
                prevLoc = combinedOffset;
            } else {
                prevLoc = currentOffset;
            }
        }
    }

    TempNode finalOffset = TempNode.newTemp();
    LocNode byteOffset = new LocNode(finalOffset);
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
