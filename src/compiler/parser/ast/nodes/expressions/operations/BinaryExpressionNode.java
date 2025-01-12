package compiler.parser.ast.nodes.expressions.operations;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.expressions.OperationNode;

/**
 * A node that represents a binary operation.
 *
 * This includes operations math operations and logical operations.
 * Example: 5 + 3, x > y, x == y, etc.
 */
public class BinaryExpressionNode implements OperationNode {
    public ExpressionNode left;
    public ExpressionNode right;
    public String operator;

    /**
     * Creates a BinaryExpressionNode with the given left and right expressions and operator.
     *
     * @param left The left operand.
     * @param right The right operand
     * @param operator The operator string (e.g. +, -, *, /, >, <, ==, etc.)
     */
    public BinaryExpressionNode(ExpressionNode left, ExpressionNode right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    /**
     * Accepts a visitor to process this node.
     *
     * @param visitor The visitor that will process this node.
     */
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
