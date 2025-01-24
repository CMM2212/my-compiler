package compiler.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to keep track of the current state of the lexer, and track information useful for
 * error messages.
 *
 * This class is responsible for keeping track of line, position, and token length information
 * related to what the lexer is currently processing. Each time the lexer processes a character, it calls
 * the advanceChar method to update the state of the lexer. Additional methods such as incrementTokenLength and
 * endLine are also used to update the state of the lexer.
 *
 * This information is used by the lexer when it needs to generate error context information during
 * exception handling.
 */
public class LexerState {
    private static final char EOF = '\0'; // End of file character
    private int line = 0; // Current line number
    private int position = 0; // Current position in the line
    private int tokenLength = 0; // Length of the current token being processed
    private final StringBuilder currentLine = new StringBuilder(); // Current line being processed
    private final List<String> lines = new ArrayList<>(); // List of all lines from the source code

    /**
     * Advances the lexer state by one character.
     *
     * @param c The next character to process.
     */
    public void advanceCharacter(char c) {
        // Update information about the current line, if the character is not EOF.
        if (c != EOF)
            currentLine.append(c);
        position++;
        tokenLength++;
        // If it is a newline character, save it as the previousLine and reset the currentLine.
        if (c == '\n') {
            lines.add(currentLine.toString());
            currentLine.setLength(0);
            line++;
            position = 0;
            tokenLength = 0;
        }
    }

    /**
     * Manually increments the token length and position by one.
     */
    public void incrementTokenLength() {
        tokenLength++;
        position++;
    }

    /**
     * Resets the current token length to 0.
     */
    public void resetTokenLength() {
        tokenLength = 0;
    }

    /**
     * Appends a newline character to the current line.
     */
    public void endLine() {
        currentLine.append('\n');
    }

    /**
     * Gets the current line number.
     *
     * @return the current line number.
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the current position in the line.
     *
     * @return the current position in the line.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Gets the length of the current token being processed.
     *
     * @return the length of the current token being processed.
     */
    public int getTokenLength() {
        return tokenLength;
    }

    /**
     * Gets the current line being processed.
     *
     * @return the current line being processed.
     */
    public String getCurrentLine() {
        return currentLine.toString();
    }

    /**
     * Gets the list of all lines from the source code that have been processed.
     *
     * @return the list of all lines from the source code.
     */
    public List<String> getLines() {
        return lines;
    }
}
