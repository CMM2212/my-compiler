package compiler.parser.ast.nodes.expressions;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.LineTrackingNode;
import compiler.parser.ast.nodes.terminals.IdNode;
import compiler.parser.ast.nodes.terminals.NumNode;

public class LocNode implements ExpressionNode, LineTrackingNode {
    public IdNode id;
    public ArrayLocNode array;

    public LocNode() {}

    public LocNode(IdNode id, ArrayLocNode array) {
        this.id = id;
        this.array = array;
    }

    public int getDepth() {
        ArrayLocNode current = array;
        int depth = 0;
        while (current != null) {
            depth++;
            current = current.array;
        }
        return depth;
    }

    public NumNode getWidthNumNode() {
        return new NumNode(this.id.getType().type.width);
    }

    public Boolean isArray() {
        return this.array != null;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}

