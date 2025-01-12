package compiler.lexer;

import compiler.lexer.tokens.Type;
import compiler.lexer.tokens.Word;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the reserved words of the language.
 *
 * This class provides a static list of keywords that are reserved in the language such
 * as true, false, if, else, while etc. It provides a way to look up the token Word by its
 * string lexeme, or to get the string lexeme from its tag.
 */
public class ReservedWords {
    private static final List<Word> keywords = new ArrayList<>();

    static {
        keywords.add(new Word("true", Tag.TRUE));
        keywords.add(new Word("false", Tag.FALSE));

        keywords.add(Type.Int);
        keywords.add(Type.Float);
        keywords.add(Type.Char);
        keywords.add(Type.Bool);

        keywords.add(new Word("if", Tag.IF));
        keywords.add(new Word("else", Tag.ELSE));
        keywords.add(new Word("while", Tag.WHILE));
        keywords.add(new Word("do", Tag.DO));
        keywords.add(new Word("break", Tag.BREAK));
        keywords.add(new Word("==", Tag.EQ));
        keywords.add(new Word("!=", Tag.NE));
        keywords.add(new Word("<", Tag.LT));
        keywords.add(new Word("<=", Tag.LE));
        keywords.add(new Word(">", Tag.GT));
        keywords.add(new Word(">=", Tag.GE));
        keywords.add(new Word("&&", Tag.AND));
        keywords.add(new Word("||", Tag.OR));
        keywords.add(new Word("+", Tag.ADD));
        keywords.add(new Word("-", Tag.SUB));
        keywords.add(new Word("*", Tag.MUL));
        keywords.add(new Word("/", Tag.DIV));
        keywords.add(new Word("=", Tag.ASSIGN));
        keywords.add(new Word("!", Tag.NOT));

        keywords.add(new Word("[", Tag.LBRACKET));
        keywords.add(new Word("]", Tag.RBRACKET));

        keywords.add(new Word(";", Tag.SEMICOLON));
        keywords.add(new Word("(", Tag.LPAREN));
        keywords.add(new Word(")", Tag.RPAREN));
        keywords.add(new Word("{", Tag.LBRACE));
        keywords.add(new Word("}", Tag.RBRACE));

        keywords.add(new Word("EOF", Tag.EOF));
    }

    /**
     * Get the Word token for the given lexeme.
     *
     * If the lexeme is not a reserved word, null is returned. This is how the lexer
     * determines if a lexeme is a reserved word or not.
     *
     * Example:
     *  ReservedWord.get("if") returns Word("if", Tag.IF)
     *
     * @param lexeme the lexeme to look up.
     * @return the Word token for the lexeme, or null if it is not a reserved word.
     */
    public static Word get(String lexeme) {
        for (Word keyword : keywords)
            if (keyword.lexeme.equals(lexeme))
                return keyword;
        return null;
    }

    /**
     * Get the string representation of the given tag.
     *
     * If the tag is associated with a reserved word, the lexeme is returned. Otherwise, the
     * character representation of the tag is returned. This helps with printing out the string
     * value of tokens by giving a tag to string conversion.
     *
     * Example:
     *  ReservedWord.get(Tag.IF) returns "if"
     *
     * @param tag the tag to look up.
     * @return the lexeme for the tag, or the character representation of the tag.
     */
    public static String get(int tag) {
        for (Word keyword : keywords)
            if (keyword.tag == tag)
                return keyword.lexeme;
        return (char) tag + "";
    }
}
