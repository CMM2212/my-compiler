package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.StatementNode;

/**
 * A node representing an if statement with an optional else statement.
 *
 * If the else statement is null, then the if statement is an if-then statement.
 *
 * Example:
 * if (x > 5) {
 *   x = x - 5;
 * } else {
 *   x = x + 5;
 * }
 */
public class IfNode implements StatementNode {
    // If (expression) { thenStatement } else { elseStatement }
    public ExpressionNode expression;
    public StatementNode thenStatement;
    public StatementNode elseStatement = null;

    /**
     * Creates an empty IfNode.
     *
     * After constructed, the expression, thenStatement, and elseStatement fields should
     * be set to the appropriate values.
     */
    public IfNode() {}

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
