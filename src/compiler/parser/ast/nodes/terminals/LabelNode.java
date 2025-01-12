package compiler.parser.ast.nodes.terminals;

import compiler.lexer.Tag;
import compiler.lexer.tokens.Word;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.StatementNode;

/**
 * A terminal node that represents a label to jump to.
 *
 * This node is not in the original language, but it is generated
 * by the intermediate code generator for helping represent control flow statements.
 * It allows goto statements to point to another line in the code.
 *
 * Example:
 * L1:     x = x + 1
 *         if  x < 10   goto L1
 */
public class LabelNode extends IdNode implements StatementNode {
    // A static counter to give each label a unique incrementing number
    static int label = 0;

    /**
     * Creates a LabelNode with the given word and id.
     *
     * This constructor should not be used directly. Instead, use the static method LabelNode.newLabel() to
     * create a new label.
     * @param word The word representing the label. (e.g. L1, L2, L3, ...)
     * @param id The identifier of the label.
     */
    public LabelNode(Word word, String id) {
        super(word, id);
    }

    /**
     * Creates a new label with a unique identifier.
     *
     * @return A new LabelNode with a unique identifier. (e.g. L1, L2, L3, ...)
     */
    public static LabelNode newLabel() {
        label++;
        return new LabelNode(new Word("L" + label, Tag.ID), "L" + label);
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
