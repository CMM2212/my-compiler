package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.StatementNode;

public class IfNode implements StatementNode {
    public ExpressionNode expression;
    public StatementNode thenStatement;
    public StatementNode elseStatement = null;

    public IfNode() {}

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
