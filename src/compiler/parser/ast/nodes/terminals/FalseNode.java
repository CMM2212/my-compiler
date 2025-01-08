package compiler.parser.ast.nodes.terminals;


import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.TerminalNode;

public class FalseNode implements TerminalNode {
    public FalseNode() {
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }

}
