package compiler.parser.ast.nodes;

import compiler.lexer.tokens.Type;
import compiler.parser.ast.nodes.declarations.TypeNode;
import compiler.symbols.Symbol;

import java.util.HashMap;

public interface ExpressionNode extends Node {
    // Static lookup table for TypedNodes' types.
    HashMap<ExpressionNode, TypeNode> typeLookup = new HashMap<>();

    default TypeNode getType() {
        return typeLookup.get(this);
    }

    default void setType(TypeNode type) {
        typeLookup.put(this, type);
    }

    default void setType(Type type) {
        typeLookup.put(this, new TypeNode(type));
    }

    default void setType(ExpressionNode node) {
        typeLookup.put(this, typeLookup.get(node));
    }

    default void setType(Symbol symbol) {
        typeLookup.put(this, symbol.type);
    }
}
