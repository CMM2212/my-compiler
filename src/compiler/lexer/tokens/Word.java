package compiler.lexer.tokens;

/**
 * Represents a word token in the source code (e.g. identifiers, reserved words).
 */
public class Word extends Token {
    public String lexeme;

    /**
     * Constructor.
     * @param s The lexeme of the word.
     * @param tag The tag for the token.
     */
    public Word(String s, int tag) {
        super(tag);
        lexeme = s;
    }

    public String toString() {
        return lexeme;
    }
}
