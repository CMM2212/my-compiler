package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.StatementNode;

public class WhileNode implements StatementNode {
    public ExpressionNode expression;
    public StatementNode body;

    public WhileNode() {
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
