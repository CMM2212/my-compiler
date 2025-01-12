package compiler.lexer.tokens;

import compiler.lexer.Tag;

/**
 * Numerical token representing an integer.
 */
public class Num extends Token {
    // The integer value of the token.
    public final int value;

    /**
     * Creates a new Num token with a given integer value.
     *
     * @param value The integer value of the token.
     */
    public Num(int value) {
        super(Tag.NUM); // Assign the tag for the token.
        this.value = value;
    }

    /**
     * Returns the integer value of the token.
     *
     * @return The integer value of the token.
     */
    @Override
    public String toString() {
        return "" + value;
    }
}
