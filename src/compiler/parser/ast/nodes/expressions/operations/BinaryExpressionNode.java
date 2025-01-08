package compiler.parser.ast.nodes.expressions.operations;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.expressions.OperationNode;


public class BinaryExpressionNode implements OperationNode {
    public ExpressionNode left;
    public ExpressionNode right;
    public String operator;

    public BinaryExpressionNode() {
    }

    public BinaryExpressionNode(ExpressionNode left, ExpressionNode right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
