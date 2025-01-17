package compiler.symbols;

import compiler.parser.ast.nodes.declarations.TypeNode;
import compiler.parser.ast.nodes.terminals.IdNode;

/**
 * A symbol representing a variable declaration.
 *
 * Stores both the type and the identifier.
 */
public class Symbol {
    public TypeNode type;
    public IdNode id;

    /**
     * Creates a symbol with the given type and identifier.
     *
     * @param type TypeNode containing the basic type and array dimensions.
     * @param id IdNode containing the identifier string.
     */
    public Symbol(TypeNode type, IdNode id) {
        this.type = type;
        this.id = id;
    }
}
