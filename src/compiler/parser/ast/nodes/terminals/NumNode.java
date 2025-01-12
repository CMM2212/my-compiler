package compiler.parser.ast.nodes.terminals;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.TerminalNode;

/**
 * A terminal number representing an integer literal.
 *
 * Example: 5
 */
public class NumNode implements TerminalNode {
    public int num ;

    /**
     * Creates an empty NumNode.
     *
     * After constructed, the num field should be set to the appropriate value.
     */
    public NumNode() {
    }

    /**
     * Creates a NumNode with the given number.
     *
     * @param num The number to store in this node as the value.
     */
    public NumNode(int num) {
        this.num = num;
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

    /**
     * Returns the number stored in this node as a string.
     *
     * @return The number stored in this node as a string.
     */
    public String toString() {
        return "" + num;
    }
}

