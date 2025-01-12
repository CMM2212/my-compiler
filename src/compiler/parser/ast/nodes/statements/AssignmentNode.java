package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.LineTrackingNode;
import compiler.parser.ast.nodes.StatementNode;
import compiler.parser.ast.nodes.expressions.LocNode;

/**
 * A node representing the assignment of a value to a variable.
 *
 * Example: x = 5; or x[100] = 3 - x[4];
 */
public class AssignmentNode implements StatementNode, LineTrackingNode {
    // left = expression
    public LocNode left;
    public ExpressionNode expression;

    /**
     * Creates an empty AssignmentNode.
     *
     * After constructed, the left and expression fields should be set to the appropriate values.
     */
    public AssignmentNode() {}

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
