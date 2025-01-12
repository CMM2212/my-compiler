package compiler.lexer.tokens;

/**
 * Represents a word token in the source code (e.g. identifiers, reserved words).
 */
public class Word extends Token {
    // The string representation of the word.
    public String lexeme;

    /**
     * Creates a new Word token with a given lexeme and tag.
     *
     * @param lexeme The lexeme of the word.
     * @param tag The tag for the token.
     */
    public Word(String lexeme, int tag) {
        super(tag);
        this.lexeme = lexeme;
    }

    /**
     * Return a string representation of the word.
     *
     * @return The lexeme of the word.
     */
    @Override
    public String toString() {
        return lexeme;
    }
}
