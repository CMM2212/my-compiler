package compiler.parser.ast.nodes.terminals;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.TerminalNode;

public class ParenthesisNode implements TerminalNode {
    public ExpressionNode expression;

    public ParenthesisNode() {
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
