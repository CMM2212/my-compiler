package compiler.lexer;

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

/**
 * Lexical analysis for the compiler.
 *
 * This class provides the lexer for the compiler which reads the input file one character at a time and
 * converts lexemes into tokens. This class is used by the parser as a way of getting each additional token
 * for the parser to handle.
 */
public class Lexer {
    private static final char EOF = '\0';
    private final LexerState state = new LexerState();
    private final String filename;
    private final BufferedInputStream bin;
    private final Set<String> OPERATOR_LEXEMES = Set.of("<", ">", "=", "!", "&", "|", "+", "-", "*", "/");
    private final Set<String> PUNCTUATION_LEXEMES = Set.of(";", "(", ")", "{", "}", "[", "]");
    public Boolean missingSemicolon = false;
    private char nextCharacter = ' ';

    /**
     * Categories of tokens that can be found in the input stream.
     */
    private enum TokenCategory {
        EOF, NUMBER, WORD, OPERATOR, PUNCTUATION
    }

    /**
     * Create a new lexer for the given file and initialize the input stream.
     *
     * @param filename the name of the file to read from.
     * @throws CompilerException if there is an error opening the file.
     */
    public Lexer(String filename) {
        this.filename = filename;
        try {
            bin = new BufferedInputStream(new FileInputStream(filename));
        } catch (IOException e) {
            throw new CompilerException("Error opening file: " + filename);
        }
    }

    /**
     * Returns the next token in the input stream.
     *
     * This method will ignore whitespace, identify the category of the next lexeme, whether it's
     * a number, word, operator, or punctuation, and then parse the value of the lexeme accordingly.
     *
     * @return the next token in the input stream.
     */
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

    /**
     * Skip whitespace characters in the input stream.
     */
    private void ignoreWhitespace() {
        while (Character.isWhitespace(nextCharacter))
            getNextCharacter();
        // Make sure the white space is not counted as part of token length.
        state.resetTokenLength();
    }

    /**
     * Determine the category of the next token in the input stream based on the next character.
     *
     * This method will return a category enum value based on the next character. If it is a digit, it'll
     * be a number. If it's a letter, it'll be word. If it's an operator, it'll be an operator. If it's
     * a punctuation, it'll be a punctuation.
     * token.
     *
     * @return the category of the next token.
     * @throws LexicalException if the next character is not a valid token.
     */
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

    /**
     * Check if the character is an operator.
     *
     * This will check if the character is in the set of operator lexemes defined within the language.
     *
     * @param character the character to check.
     * @return true if the character is an operator, false otherwise.
     */
    boolean isOperator(char character) {
        return OPERATOR_LEXEMES.contains("" + character);
    }

    /**
     * Check if the character is a punctuation.
     *
     * This will check if the character is in the set of punctuation lexemes defined within the language. This
     * includes ;, (, ), {, }, [, and ].
     *
     * @param character the character to check.
     * @return true if the character is a punctuation, false otherwise.
     */
    private boolean isPunctuation(char character) {
        return PUNCTUATION_LEXEMES.contains("" + character);
    }

    /**
     * Read the next number in the input stream which may be an integer or a real number.
     *
     * @return the next number token in the input stream.
     */
    private Token readNumber() {
        StringBuilder digits = new StringBuilder();
        boolean hasDecimal = parseNumber(digits);
        validateNumber();

        return createNumberToken(hasDecimal, digits);
    }

    /**
     * Parse the next number and update the digits StringBuilder with the number and return true if it contained
     * a decimal point.
     *
     * @param digits the StringBuilder to store the digits of the number.
     * @return true if the number contained a decimal point, false otherwise.
     */
    private boolean parseNumber(StringBuilder digits) {
        // Seeing a decimal point will signal it is a real number.
        boolean hasSeenDecimal = false;
        while (Character.isDigit(nextCharacter) || (nextCharacter == '.' && !hasSeenDecimal)) {
            if (nextCharacter == '.') hasSeenDecimal = true;
            digits.append(nextCharacter);
            getNextCharacter();
        }
        return hasSeenDecimal;
    }

    /**
     * Get the next character in the input stream and store it in the nextCharacter field.
     */
    private void getNextCharacter() {
        nextCharacter = readNextCharacter();
        // Always update the state with the new character that was read.
        state.advanceCharacter(nextCharacter);
    }

    /**
     * Read the next character in the input stream.
     *
     * @return the next character in the input stream.
     * @throws CompilerException if there is an error reading the file.
     */
    private char readNextCharacter() {
        try {
            int nextCharacterValue = bin.read();
            // Store the EOF character as the EOF constant because its value -1 cannot be cast to a char.
            if (nextCharacterValue == -1)
                return EOF;
            else
                return (char) nextCharacterValue;
        } catch (IOException e) {
            throw new CompilerException("Error reading file: " + filename);
        }
    }

    /**
     * Validates the character after the number is not something that would make the number invalid such as
     * a letter.
     *
     * @throws LexicalException if the next character is not a valid character after a number.
     */
    private void validateNumber() {
        if (!isValidEndOfNumber(nextCharacter)) {
            state.incrementTokenLength();
            throw new LexicalException("invalid decimal literal");
        }
    }

    /**
     * Check if the character is a valid character to end a number.
     *
     * For example symbols like operators or spaces or most punctuation besides . are valid:
     *  "a = 5+" "a = (5 + 5)" "a = 5;" would be valid next characters
     *  But "a = 5a" would not be valid because a cannot be the next character after a number.
     *
     * @param character the character to check.
     * @return true if the character is a valid character to end a number, false otherwise.
     */
    boolean isValidEndOfNumber(char character) {
        return isOperator(character) || character == ';' || character == ')' || character == '}' ||
                character == ']' || character == ' ' || character == '\n' || character == '\t' ||
                character == '\r';
    }

    /**
     * Creates a number token with the given digits where the number is real if hasDecimal is true, and an integer
     * otherwise.
     *
     * @param hasDecimal Did the number contain a decimal point, signaling it is real.
     * @param digits The digits of the number as a string.
     * @return the number token.
     */
    private static Token createNumberToken(boolean hasDecimal, StringBuilder digits) {
        if (hasDecimal)
            return new Real(Float.parseFloat(digits.toString()));
        else
            return new Num(Integer.parseInt(digits.toString()));
    }

    /**
     * Read a word from the input stream which may be an identifier or a reserved word.
     *
     * @return the next word token in the input stream.
     */
    private Token readWord() {
        // Read words as identifiers or reserved words
        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.append(nextCharacter);
            getNextCharacter();
        } while (Character.isLetterOrDigit(nextCharacter));

        String wordString = stringBuilder.toString();
        Word word = ReservedWords.get(wordString);

        // If it is a reserved word, return it
        if (word != null)
            return word;

        // Must be an identifier
        return new Word(wordString, Tag.ID);
    }

    /**
     * Read an operator from the input stream.
     *
     * This method will attempt to read the next two characters as an operator. If both characters make up a
     * valid operator, then it will return the two character operator, otherwise it will just return an operator
     * with the first single character.
     *
     * @return the next operator token in the input stream.
     * @throws LexicalException if the operator is not a valid reserved operator.
     */
    private Token readOperator() {
        // Get the current and next characters and combine them as operator.
        String operator = "" + nextCharacter;
        getNextCharacter();
        operator += nextCharacter;

        // Check if the operator is a valid operator.
        Word operatorToken = ReservedWords.getOperator(operator);
        if (operatorToken != null) {
            // If the found operator is two characters long, then move to the next character, because the current
            // one will be used for this operator.
            if (operatorToken.lexeme.length() == 2)
                getNextCharacter();
            return operatorToken;
        }
        throw new LexicalException("Unexpected token: " + operator);
    }

    /**
     * Read a punctuation token from the input stream.
     *
     * @return the next punctuation token in the input stream.
     */
    private Token readPunctuation() {
        Token punctuationToken = ReservedWords.get("" + nextCharacter);
        getNextCharacter();
        return punctuationToken;
    }

    /**
     * Get the error context for the current error that will provide information about the error location and
     * source code snippet.
     *
     * @return the error context for the current error.
     */
    public ErrorContext getErrorContext() {
        int errorLength = state.getTokenLength();
        int errorPosition = state.getPosition() - 1 - errorLength;

        if (nextCharacter == EOF) {
            errorLength = 1;
            errorPosition += 2;
        }

        int line = state.getLine();
        if (missingSemicolon && shouldBePreviousLine()) {
            line -= 1;
            errorPosition = state.getLines().get(line).length() - 2;
        }

        while (nextCharacter != '\n') {
            if (nextCharacter == EOF) {
                state.endLine();
                break;
            }
            getNextCharacter();
        }

        return new ErrorContext(filename, line, errorPosition, errorLength, state.getLines());
    }

    /**
     * Check if the semicolon should have been placed on the previous line.
     *
     * This operates by checking if the current line, stripped of whitespace, is the same length as the token length.
     *
     * @return true if the semicolon should have been placed on the previous line, false otherwise.
     */
    private Boolean shouldBePreviousLine() {
        int currentLineContentLength = state.getCurrentLine().strip().length();
        int currentLinLengthExpected = state.getTokenLength();
        return currentLineContentLength == currentLinLengthExpected;
    }

    /**
     * Get the current line number being read from the source code.
     *
     * @return the current line number.
     */
    public int getCurrentLine() {
        return state.getLine();
    }

    /**
     * Return a string representation of the given tag.
     *
     * @param tag The tag to convert to a string.
     * @return The string representation of the tag.
     */
    public String convertTagToString(int tag) {
        return ReservedWords.get(tag);
    }

}
