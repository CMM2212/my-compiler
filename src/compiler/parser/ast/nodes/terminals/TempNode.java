package compiler.parser.ast.nodes.terminals;

import compiler.lexer.Tag;
import compiler.lexer.tokens.Word;
import compiler.parser.ast.ASTVisitor;

public class TempNode extends IdNode {
    public static int num = 0;

    public TempNode(Word w, String id) {
        super(w, id);
    }

    public static TempNode newTemp() {
        num++;
        return new TempNode(new Word("t" + num, Tag.ID), "t" + num);
    }

    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
