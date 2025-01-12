package compiler.parser.ast.nodes.expressions.operations;

import compiler.lexer.tokens.Token;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.expressions.OperationNode;

/**
 * A node that represents a unary operation on an expression.
 *
 * Operator is stored as a token so it can be easily compared by its tag.
 *
 * Example: -5, !true
 */
public class UnaryNode implements OperationNode {
    public Token operator;
    public ExpressionNode expression;

    /**
     * Creates an empty UnaryNode.
     *
     * After constructed, the operator and expression fields should be set to the appropriate values.
     */
    public UnaryNode() {
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
