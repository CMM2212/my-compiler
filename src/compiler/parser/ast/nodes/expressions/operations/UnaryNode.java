package compiler.parser.ast.nodes.expressions.operations;

import compiler.lexer.tokens.Token;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.expressions.OperationNode;

public class UnaryNode implements OperationNode {
    public Token op;
    public ExpressionNode right;

    public UnaryNode() {

    }

    public UnaryNode(Token op, ExpressionNode right) {
        this.op = op;
        this.right = right;
    }


    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
