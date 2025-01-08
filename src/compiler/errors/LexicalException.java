package compiler.errors;

public class LexicalException extends CompilerException {
    public LexicalException(String message) {
        super(message);
    }

    public String toString() {
        return "LexicalError";
    }
}
