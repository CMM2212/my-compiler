package compiler.lexer.tokens;

import compiler.lexer.Tag;

/**
 * Real number token representing a floating point number
 */
public class Real extends Token {
    // The floating point value of the token.
    public final float value;

    /**
     * Creates a new Real token with a floating point value and the REAL tag.
     *
     * @param value The floating point value of the token.
     */
    public Real(float value) {
        super(Tag.REAL); // Assign the tag for the token.
        this.value = value;
    }

    /**
     * Returns the string representation of the token as the floating point value.
     *
     * @return The string representation of the token.
     */
    public String toString() {
        return "" + value;
    }
}
