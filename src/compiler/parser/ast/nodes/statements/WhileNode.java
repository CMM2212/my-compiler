package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.StatementNode;

/**
 * A node representing a while loop.
 *
 * Example:
 *  while (x < 10) {
 *    x = x + 1;
 *  }
 */
public class WhileNode implements StatementNode {
    // while (expression) { body }
    public ExpressionNode expression;
    public StatementNode body;

    /**
     * Creates an empty WhileNode.
     *
     * After constructed, the expression and body fields should be set to the appropriate values.
     */
    public WhileNode() {
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
