package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.StatementNode;

/**
 * A node that represents a break statement.
 *
 * Break statements are used to exit a loop early. They must be used
 * within a loop.
 *
 * Example: break;
 */
public class BreakNode implements StatementNode {
    public BreakNode() { }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
