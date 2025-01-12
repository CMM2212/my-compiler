package compiler.parser.ast.nodes.declarations;


import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.terminals.NumNode;

public class ArrayTypeNode extends TypeNode {
    public ArrayTypeNode type;
    public NumNode size;

    /**
     * Creates an empty ArrayTypeNode.
     *
     * After it is constructed, the type and size fields should be set to the appropriate values.
     */
    public ArrayTypeNode() {
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
