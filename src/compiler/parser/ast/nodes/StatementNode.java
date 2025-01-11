package compiler.parser.ast.nodes;

/**
 * A node interface that represents a statement in the AST.
 *
 * Statements are the body of the block, after the declaration. They include
 * assignments, if-else, do-while, while, break, and block statements. This extends
 * the Node interface which allows it to be visited by the ASTVisitor.
 */
public interface StatementNode extends Node {
}
