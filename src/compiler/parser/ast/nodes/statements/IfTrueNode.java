package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.StatementNode;

/**
 * A node that is used for branching the true branch of a do while loop.
 *
 * This node is not part of the original language, but is created during the intermediate
 * code generation to handle do while loops.
 *
 * Example:
 * L1:     x = x + 1
 *         if x < 10 goto L1
 */
public class IfTrueNode implements StatementNode {
    public ExpressionNode expression;

    /**
     * Creates an IfTrueNode with the given expression.
     *
     * @param expression The expression to evaluate.
     */
    public IfTrueNode(ExpressionNode expression) {
        this.expression = expression;
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
