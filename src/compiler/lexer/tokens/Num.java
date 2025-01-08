package compiler.lexer.tokens;

import compiler.lexer.Tag;

/**
 * Represents integer numbers.
 */
public class Num extends Token {
    public final int value;

    /**
     * Constructor.
     * @param v The integer value of the token.
     */
    public Num(int v) {
        super(Tag.NUM); // Assign the tag for the token.
        value = v;
    }

    @Override
    public String toString() {
        return "" + value;
    }
}
