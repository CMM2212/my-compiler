package compiler.parser.ast.nodes.declarations;

import compiler.lexer.tokens.Type;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.DeclarationNode;
import compiler.parser.ast.nodes.terminals.NumNode;

/**
 * A node that represents a type declaration.
 *
 * Type nodes are used to represent the type of variable and hold information about its
 * array dimensions. (e.g. int[3][5] would have a depth of 2 and a size of 3 and 5 for the respective dimensions)
 */
public class TypeNode implements DeclarationNode {
    public Type type;
    public ArrayTypeNode array = null;

    /**
     * Creates an empty TypeNode.
     *
     * After constructed, the type and array fields should be set to the appropriate values.
     */
    public TypeNode() {
    }

    /**
     * Creates a TypeNode with the given type.
     *
     * @param type Basic type of the variable. (e.g. int, float, char, etc.)
     */
    public TypeNode(Type type) {
        this.type = type;
    }

    /**
     * Returns true if the type is an array.
     *
     * @return true if the type is an array, false otherwise.
     */
    public Boolean isArray() {
        return array != null;
    }

    /**
     * Return the depth of the array which is the number of dimensions.
     *
     * For example, 'int[3][5]' would have a depth of 2 while 'int' would have a depth of 0.
     *
     * @return the depth of the array.
     */
    public int getDepth() {
        int depth = 0;
        for (ArrayTypeNode current = array; current != null; current = current.type)
            depth++;
        return depth;
    }

    /**
     * Get the declared size of the array at the given dimension.
     *
     * For example, 'int[3][5]' would return 3 for dimension = 0 and 5 for dimension = 1.
     *
     * @param dimension the index of the dimension to get the size of.
     * @return the size of the array at the given dimension.
     */
    public NumNode getDimensionSize(int dimension) {
        ArrayTypeNode current = array;
        for (int i = 0; i < dimension; i++)
            current = current.type;
        return current.size;
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

    /**
     * Returns a string representation of the type node.
     *
     * This does not include the array dimensions. (e.g. int[][] instead of int[3][5])
     *
     * @return a string representation of the type node.
     */
    public String toString() {
        // Add brackets for each dimension of the array.
        String arrayString = "[]".repeat(Math.max(0, getDepth()));
        return type.toString() + arrayString;
    }
}
