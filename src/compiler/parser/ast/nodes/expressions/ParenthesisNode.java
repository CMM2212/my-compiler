package compiler.parser.ast.nodes.expressions;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;

public class ParenthesisNode implements ExpressionNode {
    public ExpressionNode expression;

    public ParenthesisNode() {
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
