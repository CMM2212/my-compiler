package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.StatementNode;

/**
 * A node that is used for generating the false branch of an if statement.
 *
 * This is not a part of the original language, but is used by the intermediate code
 * generator to handle directing the flow control to the label at the end of the 'if statement' body.
 *
 * Example:
 *   L2:   iffalse x > 0  goto L1
 *         x = 1
 *   L1:
 */
public class IfFalseNode implements StatementNode {
    public ExpressionNode expression;

    /**
     * Creates an IfFalseNode with an expression.
     *
     * @param expression the expression to evaluate
     */
    public IfFalseNode(ExpressionNode expression) {
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
