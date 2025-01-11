package compiler.parser.ast.nodes;

import compiler.parser.ast.ASTVisitor;

/**
 * The base interface for all AST nodes.
 *
 * Every node in the AST must implement this interface. It provides the core method
 * for traversing the AST using the Visitor pattern, {@code accept}.
 */
public interface Node {

    /**
     * Accept a visitor to process this node.
     *
     * This method is used to implement the Visitor pattern. The visitor will call
     * the appropriate method on itself to process this node. This allows for the
     * visitor to traverse the AST.
     *
     * @param v The visitor that will process this node.
     */
    void accept(ASTVisitor v);
}
