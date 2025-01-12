package compiler.parser.ast.nodes.expressions;

import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.LineTrackingNode;

/**
 * A node interface that represents an operation in the AST.
 *
 * Binary and Unary operations both implement this interface.
 *
 * It extends ExpressionNode and LineTrackingNode so it holds a type and line number.
 */
public interface OperationNode extends ExpressionNode, LineTrackingNode {
}
