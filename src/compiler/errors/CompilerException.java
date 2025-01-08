package compiler.errors;

public class CompilerException extends RuntimeException{
    public CompilerException(String message) {
        super(message);
    }

    public String toString() {
        return "CompilerError";
    }
}
