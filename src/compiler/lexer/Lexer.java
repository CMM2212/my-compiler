package compiler.lexer;

//import compiler.errors.ErrorContext;
import compiler.errors.CompilerException;
import compiler.errors.ErrorContext;
import compiler.errors.LexicalException;
import compiler.lexer.tokens.Num;
import compiler.lexer.tokens.Real;
import compiler.lexer.tokens.Token;
import compiler.lexer.tokens.Word;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class Lexer {
    private final LexerState state = new LexerState();
    private final String filename;
    private final BufferedInputStream bin;

    private char nextCharacter = ' ';

    private enum TokenCategory {
        EOF, NUMBER, WORD, OPERATOR, PUNCTUATION
    }

    private final Set<String> OPERATOR_LEXEMES = Set.of("<", ">", "=", "!", "&", "|", "+", "-", "*", "/");
    private final Set<String> PUNCTUATION_LEXEMES = Set.of(";", "(", ")", "{", "}", "[", "]");

    private static final char EOF = '\0';

    public Boolean missingSemicolon = false;

    public Lexer(String filename) {
        this.filename = filename;
        try {
            bin = new BufferedInputStream(new FileInputStream(filename));
        } catch (IOException e) {
            throw new CompilerException("Error opening file: " + filename);
        }
    }

    public Token getNextToken() {
        ignoreWhitespace();

        TokenCategory tokenCategory = getTokenCategory();

        return switch (tokenCategory) {
            case EOF -> new Token(Tag.EOF);
            case NUMBER -> readNumber();
            case WORD -> readWord();
            case OPERATOR -> readOperator();
            case PUNCTUATION -> readPunctuation();
        };
    }

    private void ignoreWhitespace() {
        while (Character.isWhitespace(nextCharacter))
            getNextCharacter();
        state.resetTokenLength();
    }

    private void getNextCharacter() {
        nextCharacter = readNextCharacter();
        state.advanceChararacter(nextCharacter);
    }

    private char readNextCharacter() {
        try {
            int nextCharacterValue = bin.read();
            if (nextCharacterValue == -1 )
                return EOF;
            else
                return (char) nextCharacterValue;
        } catch (IOException e) {
            throw new CompilerException("Error reading file: " + filename);
        }
    }

    private TokenCategory getTokenCategory() {
        if (nextCharacter == EOF)
            return TokenCategory.EOF;
        if (Character.isDigit(nextCharacter))
            return TokenCategory.NUMBER;
        if (Character.isLetter(nextCharacter))
            return TokenCategory.WORD;
        if (isOperator(nextCharacter))
            return TokenCategory.OPERATOR;
        if (isPunctuation(nextCharacter))
            return TokenCategory.PUNCTUATION;

        state.incrementTokenLength();
        throw new LexicalException("Unexpected token: " + nextCharacter);
    }

    private Token readNumber() {
        StringBuilder digits = new StringBuilder();
        boolean hasDecimal = parseNumber(digits);
        validateNumber();

        return createNumberToken(hasDecimal, digits);
    }

    private boolean parseNumber(StringBuilder digits) {
        boolean hasSeenDecimal = false;
        while (Character.isDigit(nextCharacter) || (nextCharacter == '.' && !hasSeenDecimal)) {
            if (nextCharacter == '.')
                hasSeenDecimal = true;
            digits.append(nextCharacter);
            getNextCharacter();
        }
        return hasSeenDecimal;
    }

    private void validateNumber() {
        if (!isValidEndOfNumber(nextCharacter)) {
            state.incrementTokenLength();
            throw new LexicalException("invalid decimal literal");
        }
    }

    private static Token createNumberToken(boolean hasDecimal, StringBuilder digits) {
        if (hasDecimal)
            return new Real(Float.parseFloat(digits.toString()));
        else
            return new Num(Integer.parseInt(digits.toString()));
    }

    boolean isValidEndOfNumber(char c) {
        return isOperator(c) || c == ';' || c == ')' || c == '}' || c == ']' || c == ' ' || c == '\n' ||
                c == '\t' || c == '\r';
    }

    private Token readWord() {
        // Read words as identifiers or reserved words
        StringBuilder b = new StringBuilder();
        do {
            b.append(nextCharacter);
            getNextCharacter();
        } while (Character.isLetterOrDigit(nextCharacter));

        String s = b.toString();
        Word w = ReservedWords.get(s);

        // If it is a reserved word, return it
        if (w != null)
            return w;

        // Must be an identifier
        return new Word(s, Tag.ID);
    }

    boolean isOperator(char c) {
        return OPERATOR_LEXEMES.contains("" + c);
    }

    private Token readOperator() {
        // Read symbols as reserved words (operators)
        String operator = "" + nextCharacter;
        getNextCharacter();
        operator += nextCharacter;

        Word operatorToken = ReservedWords.getOperator(operator);
        if (operatorToken != null) {
            if (operatorToken.lexeme.length() == 2)
                getNextCharacter();
            return operatorToken;
        }
        throw new LexicalException("Unexpected token: " + operator);
    }

    private boolean isPunctuation(char peek) {
        return PUNCTUATION_LEXEMES.contains("" + peek);
    }

    private Token readPunctuation() {
        Token punctuationToken = ReservedWords.get("" + nextCharacter);
        getNextCharacter();
        return punctuationToken;
    }

    public ErrorContext getErrorContext() {
        int errorLength = state.getTokenLength();
        int errorPosition = state.getPosition() - 1 - errorLength;

        if (missingSemicolon) {
            errorLength = 1;
            errorPosition -= 1;
            if (shouldBePreviousLine()) {
                state.revertToPreviousLine();
                errorPosition = state.getCurrentLine().length() - 2;
            }
        }

        if (nextCharacter == EOF) {
            errorLength = 1;
            errorPosition += 2;
        }

        while (nextCharacter != '\n') {
            if (nextCharacter == EOF) {
                state.endLine();
                break;
            }
            getNextCharacter();
        }

        return new ErrorContext(filename, state.getLine(), errorPosition, errorLength, state.getPreviousLine(),
                state.getLines());
    }

    // Returns true if the semicolon should have been placed on the
    // previous line.
    private Boolean shouldBePreviousLine() {
        int currentLineContentLength = state.getCurrentLine().strip().length();
        int currentLinLengthExpected = state.getTokenLength();
        return currentLineContentLength == currentLinLengthExpected;
    }

    public int getCurrentLine() {
        return state.getLine();
    }

    public String convertTagToString(int tag) {
        return ReservedWords.get(tag);
    }
}
