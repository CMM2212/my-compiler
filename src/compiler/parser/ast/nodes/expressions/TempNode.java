package compiler.parser.ast.nodes.expressions;

import compiler.lexer.Tag;
import compiler.lexer.tokens.Word;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.terminals.IdNode;

/**
 * A node that represents a temporary node for intermediate code generation.
 *
 * This node is used to store temporary variables that are generated during the
 * intermediate code generation process. Because it is a terminal node representing a
 * location, it extends LocNode.
 *
 * Example:
 *   x = 5 + 3 + 4; becomes t1 = 5 + 3; x = t1 + 4; with t1 being a TempNode.
 */
public class TempNode extends LocNode {
    // A static counter to give each temporary variable a unique incrementing number
    // as an identifier (e.g. t1, t2, t3, ...).
    public static int num = 0;

    /**
     * Creates a TempNode with the given word and id.
     *
     * This constructor should not be used directly. Instead, use the static method TempNode.newTemp() to
     * create a new temporary variable.
     *
     * @param word The word representing the temporary variable.
     * @param id The identifier of the temporary variable.
     */
    public TempNode(Word word, String id) {
        this.id = new IdNode(word, id);
    }

    /**
     * Creates a new temporary variable with a unique identifier.
     *
     * @return A new TempNode with a unique identifier. (e.g. t1, t2, t3, ...)
     */
    public static TempNode newTemp() {
        num++;
        return new TempNode(new Word("t" + num, Tag.ID), "t" + num);
    }

    /**
     * Returns the string representation of the temporary variable.
     *
     * @return The string representation of the temporary variable.
     */
    public String toString() {
        return id.toString();
    }

    /**
     * Accepts a visitor to process this node.
     *
     * @param visitor The visitor that will process this node.
     */
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
