package compiler.parser.ast.nodes.expressions;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.LineTrackingNode;
import compiler.parser.ast.nodes.terminals.IdNode;

public class LocNode implements ExpressionNode, LineTrackingNode {
    public IdNode id;
    public ArrayLocNode array;

    public LocNode() {}

    public LocNode(IdNode id) {
        this.id = id;
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

    public int getDimensionSize() {
        if (array == null) {
            return 0;
        }
        return this.id.getType().array.size.num;
    }

    public int getWidth() {
        return this.id.getType().type.type.width;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}

