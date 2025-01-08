package compiler.parser.ast.nodes.terminals;

import compiler.lexer.tokens.Word;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.TerminalNode;

public class IdNode implements TerminalNode {
    public Word w;
    public String id;

    public IdNode () {
    }

    public IdNode (Word w, String id) {
        this.w = w;
        this.id = id;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }

}