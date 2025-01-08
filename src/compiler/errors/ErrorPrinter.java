package compiler.errors;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ErrorPrinter {
    private static final String BLUE = "\u001B[34m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    public void printError(ErrorContext context, CompilerException e) {
        System.out.println(
                BLUE +
                     "  File \"" + context.filename +"\", line " +
                GREEN +
                    context.line +
                BLUE +
                    " position " +
                GREEN +
                    context.position + "\n    " +
                RED
                    + context.lineText +
                RESET +
                    "    " + " ".repeat(context.position) + "^".repeat(context.length) + "\n" +
                RED +
                    e + ": " +
                RESET
                    + e.getMessage()
        );
    }

    public void printError(List<String> lines, ErrorContext context, TypeException e) {
        int targetLine = e.line;
        int firstLine = max(targetLine - 2, 0);
        int lastLine = min(targetLine + 2, lines.size());
        System.out.println(
                BLUE +
                    "  File \"" + context.filename +"\", line " +
                RESET +
                        (targetLine + 1)
        );
        for (int i = firstLine; i < targetLine; i++) {
            System.out.print(
                    GREEN +
                            "      " + (i + 1) + " " +
                    RESET +
                            lines.get(i)
            );
        }
        System.out.print(
                GREEN +
                    "----> " + (targetLine + 1) + " " +
                RED +
                    lines.get(targetLine)
        );
        for (int i = targetLine + 1; i < lastLine; i++) {
            System.out.println(
                    GREEN +
                            "      " + (i + 1) + " " +
                    RESET +
                            lines.get(i)
            );
        }

        System.out.println(
                RED +
                    e + ": " +
                RESET
                    + e.getMessage()
        );
    }
}
