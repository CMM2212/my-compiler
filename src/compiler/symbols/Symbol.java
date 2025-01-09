package compiler.symbols;

import compiler.parser.ast.nodes.declarations.TypeNode;
import compiler.parser.ast.nodes.terminals.IdNode;

public class Symbol {
    public TypeNode type;
    public IdNode id;

    public Symbol(TypeNode type, IdNode id) {
        this.type = type;
        this.id = id;
    }
}
