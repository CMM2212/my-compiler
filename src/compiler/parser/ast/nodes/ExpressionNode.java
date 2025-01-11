package compiler.parser.ast.nodes;

import compiler.lexer.tokens.Type;
import compiler.parser.ast.nodes.declarations.TypeNode;
import compiler.symbols.Symbol;

import java.util.HashMap;

/**
 * Interface for all nodes that represent expressions.
 *
 * Expressions are nodes which can be evaluated for a value and have an associated type. This
 * interface provides a way to store and retrieve the type of expression node. The TypeNode it
 * stores allows access to the basic type of the expression, as well as any array dimensions.
 *
 * This interface is important for the type checker. It allows the type checker to get and set the
 * type of each node as it traverses the tree.
 *
 * There are a few overloaded methods for setting the type in order to make it easier to use.
 */
public interface ExpressionNode extends Node {
    // Static lookup table for the TypeNode associated with an ExpressionNode.
    HashMap<ExpressionNode, TypeNode> typeLookup = new HashMap<>();

    /**
     * Get the type of this expression node.
     *
     * @return the TypeNode associated with this expression node.
     */
    default TypeNode getType() {
        return typeLookup.get(this);
    }

    /**
     * Set the type of this expression node.
     *
     * @param type the TypeNode to associate with this expression node.
     */
    default void setType(TypeNode type) {
        typeLookup.put(this, type);
    }

    /**
     * Set the type of this expression node.
     *
     * @param type Basic type of the expression.
     */
    default void setType(Type type) {
        typeLookup.put(this, new TypeNode(type));
    }

    /**
     * Set the type of this expression node.
     *
     * @param node the ExpressionNode to get the type from.
     */
    default void setType(ExpressionNode node) {
        typeLookup.put(this, typeLookup.get(node));
    }

    /**
     * Set the type of this expression node.
     *
     * @param symbol the Symbol to get the type from.
     */
    default void setType(Symbol symbol) {
        typeLookup.put(this, symbol.type);
    }
}
