package compiler.parser.ast.nodes.terminals;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.TerminalNode;

/**
 * A terminal node that represents the boolean literal true.
 */
public class TrueNode implements TerminalNode {
    /**
     * Creates a TrueNode.
     */
    public TrueNode() {
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
