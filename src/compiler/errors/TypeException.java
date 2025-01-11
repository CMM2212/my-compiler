package compiler.errors;

/**
 * Exception thrown during the type checking phase of the compiler.
 *
 * This is for things like type mismatches, or improper dimensional access of arrays.
 */
public class TypeException extends CompilerException {
    // The line number in the source code where the error occurred.
    public int line;

    /**
     * Create a new TypeException with a message.
     *
     * @param message The message explaining the error that occurred.
     * @param line The line number in the source code where the error occurred.
     */
    public TypeException(String message, int line) {
        super(message);
        this.line = line;
    }

    /**
     * Return a string representation of the exception.
     *
     * @return The string "TypeError".
     */
    public String toString() {
        return "TypeError";
    }
}
