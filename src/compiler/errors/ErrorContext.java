package compiler.errors;

import java.util.List;

/**
 * Data structure for storing information about the context of an error in the source code.
 *
 * This information is retrieved from the lexer and parser and is used to provide more detailed
 * information about the location of an error in the source code. This allows for the error message
 * to show the source code snippet where the error occurred and point to the problem directly.
 */
public class ErrorContext {
    public String filename;
    public int line;
    public int position;
    public int length;
    public String lineText;
    public List<String> lines;

    /**
     * Create a new ErrorContext with the given information.
     *
     * @param filename The name of the file where the error occurred.
     * @param line The line number where the error occurred.
     * @param position The position in the line where the error occurred.
     * @param length The length of the error in the line (e.g. "533.5" would have a length of 5).
     * @param lineText The text of the line where the error occurred.
     * @param lines All the lines of text from the file where the error occurred.
     */
    public ErrorContext(String filename, int line, int position, int length, List<String> lines) {
        this.filename = filename;
        this.line = line;
        this.position = position;
        this.length = length;
        this.lines = lines;
    }
}
