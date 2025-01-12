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
    private int line = 0; // Current line number
    private int position = 0; // Current position in the line
    private int tokenLength = 0; // Length of the current token being processed
    private StringBuilder currentLine = new StringBuilder(); // Current line being processed
    private String previousLine = ""; // Previous line that was processed
    private final List<String> lines = new ArrayList<>(); // List of all lines from the source code

    /**
     * Advances the lexer state by one character.
     *
     * @param c The next character to process.
     */
    public void advanceChar(char c) {
        // Update information about the current line.
        currentLine.append(c);
        position++;
        tokenLength++;
        // If it is a newline character, save it as the previousLine and reset the currentLine.
        if (c == '\n') {
            lines.add(currentLine.toString());
            previousLine = currentLine.toString();
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
     * Moves the lexer state back to the previous line.
     *
     * Useful for when a missing semicolon is detected only after it has moved to the next line.
     * This allows it to revert back to the previous line and point to the end of the line
     * where the semicolon is actually missing.
     */
    public void revertToPreviousLine() {
        line--;
        currentLine = new StringBuilder(previousLine);
        position = currentLine.length();
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
     * Gets the previous line that was processed.
     *
     * @return the previous line that was processed.
     */
    public String getPreviousLine() {
        return previousLine;
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
