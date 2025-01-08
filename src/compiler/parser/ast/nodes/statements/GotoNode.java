package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.StatementNode;
import compiler.parser.ast.nodes.terminals.LabelNode;

public class GotoNode implements StatementNode {
    public LabelNode label;

    public GotoNode(LabelNode label) {
        this.label = label;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
