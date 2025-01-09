package compiler.parser.ast.nodes.terminals;

import compiler.lexer.Tag;
import compiler.lexer.tokens.Word;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.expressions.LocNode;

public class TempNode extends LocNode {
    public static int num = 0;

    public TempNode(Word w, String id) {
        this.id = new IdNode(w, id);
    }

    public static TempNode newTemp() {
        num++;
        return new TempNode(new Word("t" + num, Tag.ID), "t" + num);
    }

    public String toString() {
        return id.toString();
    }

    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
