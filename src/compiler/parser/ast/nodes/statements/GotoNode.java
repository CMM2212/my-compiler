package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.StatementNode;
import compiler.parser.ast.nodes.terminals.LabelNode;

/**
 * A node that represents a goto statement.
 *
 * It tells the program to jump to a specific label.
 *
 * This node is not in the original language, but it is generated
 * by the intermediate code generator for helping represent control flow statements.
 *
 * Example:
 *   L2:   iffalse x > 0  goto L1
 *         x = 1
 *   L1:
 */
public class GotoNode implements StatementNode {
    public LabelNode label;

    /**
     * Creates a GotoNode with the given label.
     *
     * @param label The label to jump to.
     */
    public GotoNode(LabelNode label) {
        this.label = label;
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
