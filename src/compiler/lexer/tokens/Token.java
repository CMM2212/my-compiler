package compiler.lexer.tokens;

/**
 * Represents a single lexical unit in the source code.
 */
public class Token {
    public final int tag;

    /**
     * Constructor.
     * @param t The tag for the token.
     */
    public Token(int t) {
        tag = t;
    }

    public String toString() {
        return "" + (char)tag;
    }
}
