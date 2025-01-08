package compiler.parser.ast.nodes;

import compiler.parser.ast.nodes.declarations.TypeNode;

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
}
