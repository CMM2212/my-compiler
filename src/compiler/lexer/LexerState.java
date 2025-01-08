package compiler.lexer;

import java.util.ArrayList;
import java.util.List;

public class LexerState {
    private int line = 0;
    private int position = 0;
    private int tokenLength = 0;
    private StringBuilder currentLine = new StringBuilder();
    private String previousLine = "";
    private final List<String> lines = new ArrayList<>();

    public void advanceChar(char c) {
        currentLine.append(c);
        position++;
        tokenLength++;
        if (c == '\n') {
            lines.add(currentLine.toString());
            previousLine = currentLine.toString();
            currentLine.setLength(0);
            line++;
            position = 0;
            tokenLength = 0;
        }
    }

    public void incrementTokenLength() {
        tokenLength++;
        position++;
    }

    public void resetTokenLength() {
        tokenLength = 0;
    }

    public void setTokenLength(int length) {
        tokenLength = length;
    }

    public void endLine() {
        currentLine.append('\n');
    }

    public void revertToPreviousLine() {
        line--;
        currentLine = new StringBuilder(previousLine);
        position = currentLine.length();
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

    public int getTokenLength() {
        return tokenLength;
    }

    public String getCurrentLine() {
        return currentLine.toString();
    }

    public String getPreviousLine() {
        return previousLine;
    }

    public List<String> getLines() {
        return lines;
    }
}
