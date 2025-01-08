package compiler.errors;

public class TypeException extends CompilerException {
    public int line;

    public TypeException(String message, int line) {
        super(message);
        this.line = line;
    }

    public String toString() {
        return "TypeError";
    }
}
