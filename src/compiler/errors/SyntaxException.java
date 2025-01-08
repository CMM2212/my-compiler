package compiler.errors;

public class SyntaxException extends CompilerException {
    public SyntaxException(String message) {
        super(message);
    }

    public String toString() {
        return "SyntaxError";
    }
}
