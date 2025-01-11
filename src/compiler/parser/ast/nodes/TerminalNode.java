package compiler.parser.ast.nodes;

/**
 * A node interface that represents a terminal node in the AST.
 *
 * Terminal nodes are the leaf nodes of the AST. They include identifiers, literals,
 * and labels. They extend the ExpressionNode interface which allows them to be visited
 * and allows them to store a type.
 */
public interface TerminalNode extends ExpressionNode {
}
