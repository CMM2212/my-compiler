package compiler.parser.ast.nodes.expressions;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;

/**
 * A node that represents a parenthesized expression.
 *
 * Example: (x + 3)
 */
public class ParenthesisNode implements ExpressionNode {
    public ExpressionNode expression;

    /**
     * Creates an empty ParenthesisNode.
     *
     * After constructed, the expression field should be set to the appropriate value.
     */
    public ParenthesisNode() {
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
