package compiler.lexer.tokens;

/**
 * Tokens are the base class for all lexical units in the source code.
 *
 * They represent units such as keywords, identifiers, operators, literals, etc. and each
 * have a tag that represents the type of token.
 */
public class Token {
    // The tag for the token. (e.g. Num, Id, If, While, etc.)
    public final int tag;

    /**
     * Creates a new token
     *
     * @param tag The tag for the token.
     */
    public Token(int tag) {
        this.tag = tag;
    }

    /**
     * Returns a string representation of the token by converting the tag to a character.
     *
     * @return The string representation of the token.
     */
    public String toString() {
        return "" + (char)tag;
    }
}
