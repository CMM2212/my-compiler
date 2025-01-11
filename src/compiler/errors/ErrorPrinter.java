package compiler.errors;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Utility class for printing compiler error messages cleanly.
 *
 * This provides colored formatting and context for error messaging when printing
 * compiler errors to the console. For lexical/syntax errors it prints the line and
 * underlines the problem, and for type errors it prints the surrounding lines as well.
 */
public class ErrorPrinter {
    // ANSI escape codes for colored output
    private static final String BLUE = "\u001B[34m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    /**
     * Print a lexical or syntax error message to the console.
     *
     * This will print the filename, line number, position, and underline the token that caused the error.
     * Additionally, it will print the type of error and the error message.
     *
     * Example:
     *  File "input.txt", line 4 position 8
     *         a = 1.b;
     *             ^^^
     * LexicalError: invalid decimal literal
     *
     * @param context The context of the error.
     * @param e The exception that was thrown.
     */
    public void printError(ErrorContext context, CompilerException e) {
        // Print error message and use ANSI escape codes for colored output.
        // Example string results are placed in comments for reference.
        System.out.println(
                BLUE +
                     "  File \"" + context.filename +"\", line " + // File "input.txt", line
                GREEN +
                    context.line + // 4
                BLUE +
                    " position " + // position
                GREEN +
                    context.position + "\n    " + // 8
                RED
                    + context.lineText + // a = 1.b;
                RESET +
                    "    " + " ".repeat(context.position) + "^".repeat(context.length) + "\n" + //        ^^^
                RED +
                    e + ": " + // LexicalError:
                RESET
                    + e.getMessage() // invalid decimal literal
        );
    }

    /**
     * Prints a type error message to the console.
     *
     * This will print the filename, line number, and the surrounding lines of the error. The
     * line containing the error will have an arrow pointed to it. Additionally, it will print
     * the type of error and the error message.
     *
     * Example:
     *   File "input.txt", line 6
     *       4
     *       5     b = a + 1;
     * ----> 6     a = b + 1;
     *       7     a = a + 5;
     *
     * TypeError: type mismatch: cannot assign 'float' to 'int'
     *
     * @param lines A list of strings from the source code representing each line.
     * @param context The context of the error.
     * @param e The exception that was thrown.
     */
    public void printError(List<String> lines, ErrorContext context, TypeException e) {
        // Retrieve the target line and calculate the surrounding lines to print, making sure to
        // not go out of bounds by using min/max.
        int targetLine = e.line;
        int firstLine = max(targetLine - 2, 0);
        int lastLine = min(targetLine + 3, lines.size());

        // Print error message and use ANSI escape codes for colored output.
        // Example string results are placed in comments for reference.
        System.out.println(
                BLUE +
                    "  File \"" + context.filename +"\", line " + // File "input.txt", line
                RESET +
                        (targetLine + 1) // 6
        );

        // Print the preceding lines and their line number.
        for (int i = firstLine; i < targetLine; i++) {
            System.out.print(
                    GREEN +
                            "      " + (i + 1) + " " +  //      4
                    RESET +
                            lines.get(i) // 5    b = a + 1
            );
        }

        // Print the target line with an arrow pointing to it.
        System.out.print(
                GREEN +
                    "----> " + (targetLine + 1) + " " + // ----> 6
                RED +
                    lines.get(targetLine) // a = b + 1;
        );

        // Print the lines after the target line.
        for (int i = targetLine + 1; i < lastLine; i++) {
            System.out.println(
                    GREEN +
                            "      " + (i + 1) + " " + //      7
                    RESET +
                            lines.get(i) // a = a + 5
            );
        }

        System.out.println(
                RED +
                    e + ": " + // TypeError:
                RESET
                    + e.getMessage() // type mismatch: cannot assign 'float' to 'int'
        );
    }
}
