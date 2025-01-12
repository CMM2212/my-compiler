package compiler.parser.ast.nodes.terminals;

import compiler.lexer.tokens.Token;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.TerminalNode;

/**
 * A terminal node that represents a real number (float).
 *
 * Example: 1.86
 */
public class RealNode implements TerminalNode {
    public float value;

    /**
     * Creates an empty RealNode.
     *
     * After constructed, the value field should be set to the appropriate value.
     */
    public RealNode() {}

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
     * Returns the string representation of the real number.
     *
     * @return The string representation of the real number.
     */
    public String toString() {
        return Float.toString(value);
    }
}
