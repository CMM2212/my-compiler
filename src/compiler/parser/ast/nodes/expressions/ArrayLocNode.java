package compiler.parser.ast.nodes.expressions;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;

/**
 * A node that represents an array location.
 *
 * This node is only attached to LocNodes and other ArrayLocNodes. It extends LocNode by
 * adding an expression field that represents the index of the array.
 *
 * Example: the [3] in x[3]
 */
public class ArrayLocNode extends LocNode {
    public ExpressionNode expression;

    /**
     * Creates an empty ArrayLocNode.
     *
     * After constructed, the expression field should be set to the appropriate value.
     */
    public ArrayLocNode() {
    }

    /**
     * Creates an ArrayLocNode with an array and an expression.
     *
     * @param array an additional array location
     * @param expression the index of the array
     */
    public ArrayLocNode(ArrayLocNode array, ExpressionNode expression) {
        this.array = array;
        this.expression = expression;
    }

    /**
     * Accepts a visitor to process this node.
     *
     * @param visitor The visitor that will process this node.
     */
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
