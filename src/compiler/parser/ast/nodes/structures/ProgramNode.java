package compiler.parser.ast.nodes.structures;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.Node;
import compiler.parser.ast.nodes.statements.BlockNode;

/**
 * The top-level node in the AST representing the entire program.
 */
public class ProgramNode implements Node {
    // { block }
    public BlockNode block;

    /**
     * Creates an empty ProgramNode.
     *
     * After constructed, the block field should be set to the appropriate value.
     */
    public ProgramNode() {}

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
