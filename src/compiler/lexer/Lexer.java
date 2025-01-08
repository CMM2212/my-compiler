package compiler.lexer;

//import compiler.errors.ErrorContext;
import compiler.errors.ErrorContext;
import compiler.errors.LexicalException;
import compiler.lexer.tokens.Num;
import compiler.lexer.tokens.Real;
import compiler.lexer.tokens.Token;
import compiler.lexer.tokens.Word;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Lexer {
    private final LexerState state = new LexerState();
    private final String filename;
    private BufferedInputStream bin;

    private char peek = ' ';

    private static final char EOF = '\0';

    public Boolean missingSemicolon = false;

    public Lexer(String filename) {
        this.filename = filename;
        try {
            bin = new BufferedInputStream(new FileInputStream(filename));
        } catch (IOException e) {
            System.out.println("Error opening file: " + filename);
        }
    }

    void readChar() throws IOException {
        int value = bin.read();
        if (value == -1) {
            peek = EOF;
            return;
        }
        char c = (char) value;
        state.advanceChar(c);
        peek = c;
    }

    public Token scan() throws IOException {
        ignoreWhitespace();

        if ((int)peek == 0) {
            return new Token(Tag.EOF);
        } else if (Character.isDigit(peek)) {
            return readNumber();
        } else if (Character.isLetter(peek)) {
            return readWord();
        } else if (isOperator(peek)) {
            return readOperator();
        } else if (isPunctuation(peek)) {
            return readPunctuation();
        }

        Token t = new Token(peek);
        state.incrementTokenLength();
        throw new LexicalException("Unexpected token: " + t);
    }

    private void ignoreWhitespace() throws IOException {
        while (Character.isWhitespace(peek)) {
            readChar();
        }
        state.resetTokenLength();
    }

    private Token readNumber() throws IOException {
        StringBuilder digits = new StringBuilder();

        // Handle integer part first
        do {
            digits.append(peek);
            readChar();
        } while (Character.isDigit(peek));


        // If is integer, return Num
        if (peek != '.') {
            if (isValidEndOfNumber(peek)) {
                return new Num(Integer.parseInt(digits.toString()));
            } else {
                state.incrementTokenLength();
                throw new LexicalException("invalid decimal literal");
            }
        }

        // Add decimal point and then if there are any digits after the decimal point, add those.
        do {
            digits.append(peek);
            readChar();
        } while (Character.isDigit(peek));

        // If the next character is a valid character to follow a number, return a Real
        if (isValidEndOfNumber(peek)) {
            return new Real(Float.parseFloat(digits.toString()));
        } else {
            state.incrementTokenLength();
            throw new LexicalException("invalid decimal literal");
        }
    }

    boolean isValidEndOfNumber(char c) {
        return isOperator(c) || c == ';' || c == ')' || c == '}' || c == ']' || c == ' ' || c == '\n' ||
                c == '\t' || c == '\r';
    }

    private Token readWord() throws IOException {
        // Read words as identifiers or reserved words
        StringBuilder b = new StringBuilder();
        do {
            b.append(peek);
            readChar();
        } while (Character.isLetterOrDigit(peek));

        String s = b.toString();
        Word w = ReservedWords.get(s);

        // If it is a reserved word, return it
        if (w != null)
            return w;

        // Must be an identifier
        return new Word(s, Tag.ID);
    }

    boolean isOperator(char c) {
        return c == '<' || c == '>' || c == '=' || c == '!' || c == '&' || c == '|'
                || c == '+' || c == '-' || c == '*' || c == '/';
    }

    private Token readOperator() throws IOException {
        // Read symbols as reserved words (operators)
        StringBuilder b = new StringBuilder();
        b.append(peek);
        readChar();
        // Attempt to handle double symbol operators
        // If not, just use the first symbol.
        if (ReservedWords.get(b.toString() + peek) != null) {
            b.append(peek);
            readChar();
        }

        String s = b.toString();
        Word w = ReservedWords.get(s);

        if (w != null) {
            return w;
        } else {
            throw new LexicalException("Unexpected token: " + b);
        }
    }

    private boolean isPunctuation(char peek) {
        return peek == ';' || peek == '(' || peek == ')' || peek == '{' || peek == '}'
                || peek == '[' || peek == ']';
    }

    private Token readPunctuation() throws IOException {
        Token t = ReservedWords.get("" + peek);
        readChar();
        return t;
    }

    public ErrorContext getErrorContext() throws IOException {
        int errorLength = state.getTokenLength();
        int errorPosition = state.getPosition() - 1 - errorLength;

        if (missingSemicolon) {
            if (shouldBePreviousLine()) {
                state.revertToPreviousLine();
                errorLength = 1;
                errorPosition = state.getCurrentLine().length() - 2;
                peek = '\n';
            } else {
                errorLength = 1;
                errorPosition -= 2;
            }
        }

        if (peek == EOF) {
            errorLength = 1;
            errorPosition += 2;
        }

        while (peek != '\n') {
            if (peek == EOF) {
                state.endLine();
                break;
            }
            readChar();
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
