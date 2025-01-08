package compiler.parser.ast.nodes.terminals;

import compiler.lexer.Tag;
import compiler.lexer.tokens.Word;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.StatementNode;

public class LabelNode extends IdNode implements StatementNode {
    static int label = 0;

    public LabelNode(Word w, String id) {
        super(w, id);
    }

    public static LabelNode newLabel() {
        label++;
        return new LabelNode(new Word("L" + label, Tag.ID), "L" + label);
    }

    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
