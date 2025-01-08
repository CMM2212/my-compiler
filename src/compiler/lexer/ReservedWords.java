package compiler.lexer;

import compiler.lexer.tokens.Type;
import compiler.lexer.tokens.Word;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the reserved words of the language.
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

    public static Word get(String lexeme) {
        for (Word keyword : keywords)
            if (keyword.lexeme.equals(lexeme))
                return keyword;
        return null;
    }

    public static String get(int tag) {
        for (Word keyword : keywords)
            if (keyword.tag == tag)
                return keyword.lexeme;
        return (char) tag + "";
    }
}
