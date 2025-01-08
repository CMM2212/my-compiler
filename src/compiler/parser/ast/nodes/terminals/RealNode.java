package compiler.parser.ast.nodes.terminals;

import compiler.lexer.tokens.Token;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.TerminalNode;

public class RealNode implements TerminalNode {
    public float value;
    public Token t;

    public RealNode() {}

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }

    public String toString() {
        return Float.toString(value);
    }
}
