package compiler.parser.ast.nodes.expressions;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.terminals.IdNode;

public class ArrayLocNode extends LocNode {
    public ArrayLocNode array;
    public ExpressionNode expression;

    public ArrayLocNode() {
    }

    public ArrayLocNode(ArrayLocNode array, ExpressionNode expression) {
        this.array = array;
        this.expression = expression;
    }

    public ArrayLocNode(ArrayLocNode array, ExpressionNode expression, IdNode id) {
        this.expression = expression;
        this.array = array;
        this.id = id;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
