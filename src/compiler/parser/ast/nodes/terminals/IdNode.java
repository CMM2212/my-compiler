package compiler.parser.ast.nodes.terminals;

import compiler.lexer.tokens.Word;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.TerminalNode;

/**
 * A terminal node that represents an identifier.
 *
 * This node contains the identifier string for
 *
 * Example: x
 */
public class IdNode implements TerminalNode {
    // Word is used for looking up identifiers in the symbol table
    public Word word;
    public String id;

    /**
     * Creates an empty IdNode.
     *
     * After constructed, the id field should be set to the appropriate value.
     */
    public IdNode () {
    }

    /**
     * Creates an IdNode with a word and an id.
     *
     * @param word the word token for the identifier
     * @param id the identifier string
     */
    public IdNode (Word word, String id) {
        this.word = word;
        this.id = id;
    }

    /**
     * Returns the string representation of the identifier.
     *
     * @return the string representation of the identifier
     */
    public String toString() {
        return id;
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