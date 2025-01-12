package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.StatementNode;

/**
 * A node that represents a do-while loop.
 *
 * Example:
 *  do {
 *    x = x + 1;
 *  while (x < 10);
 */
public class DoWhileNode implements StatementNode {
    // do { body } while (expression);
    public ExpressionNode expression;
    public StatementNode body;

    /**
     * Creates an empty DoWhileNode.
     *
     * After constructed, the expression and body fields should be set to the appropriate values.
     */
    public DoWhileNode() {
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
