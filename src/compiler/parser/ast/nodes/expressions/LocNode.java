package compiler.parser.ast.nodes.expressions;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.LineTrackingNode;
import compiler.parser.ast.nodes.terminals.IdNode;
import compiler.parser.ast.nodes.terminals.NumNode;

/**
 * A node that represents a location in the AST.
 *
 * These are variable references that are an identifier and optional array references. They
 * are used within the body of expressions and statements, as opposed to TypeNode which is used in
 * declarations.
 *
 * The ArrayLocNode is responsible for handling the information about the array references.
 *
 * Example: x or x[3][5]
 */
public class LocNode implements ExpressionNode, LineTrackingNode {
    public IdNode id;
    public ArrayLocNode array;

    /**
     * Creates an empty LocNode.
     *
     * After constructed, the id and array fields should be set to the appropriate values.
     */
    public LocNode() {}

    /**
     * Creates a LocNode with the given id and array.
     *
     * @param id The identifier of the location.
     * @param array The array reference of the location.
     */
    public LocNode(IdNode id, ArrayLocNode array) {
        this.id = id;
        this.array = array;
    }

    /**
     * Gives the number of dimensions of the array.
     *
     * @return The number of dimensions of the array.
     */
    public int getDepth() {
        int depth = 0;
        for (ArrayLocNode current = this.array; current != null; current = current.array)
            depth++;
        return depth;
    }

    /**
     * Utility method that returns a NumNode with the width (size) of this type.
     *
     * This is useful for intermediate code generation when dealing with array offsets.
     *
     * @return A NumNode with the width (size) of this type.
     */
    public NumNode getWidthNumNode() {
        return new NumNode(this.id.getType().type.width);
    }

    /**
     * Returns true if the location is an array.
     * @return true if the location is an array, false otherwise.
     */
    public Boolean isArray() {
        return this.array != null;
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

