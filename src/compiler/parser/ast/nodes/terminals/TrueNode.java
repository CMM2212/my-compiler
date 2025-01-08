package compiler.parser.ast.nodes.terminals;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.TerminalNode;

public class TrueNode implements TerminalNode {
    public TrueNode() {
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
