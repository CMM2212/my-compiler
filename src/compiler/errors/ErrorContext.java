package compiler.errors;


import java.util.List;

public class ErrorContext {
    public String filename;
    public int line;
    public int position;
    public int length;
    public String lineText;
    public List<String> lines;

    public ErrorContext(String filename, int line, int position, int length, String lineText, List<String> lines) {
        this.filename = filename;
        this.line = line;
        this.position = position;
        this.length = length;
        this.lineText = lineText;
        this.lines = lines;
    }
}
