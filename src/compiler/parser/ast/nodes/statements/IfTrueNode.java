package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.StatementNode;

public class IfTrueNode implements StatementNode {
    public ExpressionNode expression;

    public IfTrueNode(ExpressionNode expression) {
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
