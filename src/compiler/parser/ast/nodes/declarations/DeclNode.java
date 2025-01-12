package compiler.parser.ast.nodes.declarations;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.DeclarationNode;
import compiler.parser.ast.nodes.terminals.IdNode;

/**
 * A node that represents a declaration of a variable.
 *
 * This includes both the type and the identifier (e.g. int x; or float[3][5] y;)
 */
public class DeclNode implements DeclarationNode {
    public TypeNode type;
    public IdNode id;

    /**
     * Creates an empty DeclNode.
     *
     * After constructed, the type and id fields should be set to the appropriate values.
     */
    public DeclNode() {}

    /**
     * Accepts a visitor to process this node.
     *
     * @param visitor The visitor that will process this node.
     */
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
