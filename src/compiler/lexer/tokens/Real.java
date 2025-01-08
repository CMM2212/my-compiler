package compiler.lexer.tokens;

import compiler.lexer.Tag;

/**
 * Represents a floating point number.
 */
public class Real extends Token {
    public final float value;

    /**
     * Constructor.
     * @param v The floating point value of the token.
     */
    public Real(float v) {
        super(Tag.REAL); // Assign the tag for the token.
        value = v;
    }

    public String toString() {
        return "" + value;
    }
}
