package compiler.parser.ast.nodes.terminals;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.TerminalNode;

public class NumNode implements TerminalNode {
    public int num ;

    public NumNode() {

    }

    public NumNode(int num) {
        this.num = num;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }

    public String toString() {
        return "" + num;
    }
}

