package compiler.symbols;

import compiler.lexer.tokens.Token;
import compiler.lexer.tokens.Word;
import compiler.parser.ast.nodes.declarations.DeclNode;

import java.util.Hashtable;

/**
 * Symbol table for storing variable declarations.
 *
 * This class allows each block to have its own scope containing symbols.
 * When an IdNode is encountered, the symbol table will be queried to find if
 * the identifier has been declared in the current scope. If not, it will
 * continue to look up higher scopes until it finds the identifier. If it
 * can't find one, it will return null.
 *
 * Variables are stored in the symbol table when the declarations are traversed
 * in the Parser, and retrieved when IdNodes are encountered in the Parser and
 * TypeChecker.
 */
public class SymbolTable {
    Hashtable<String, Symbol> table;
    // Previous symbol table from the outer scope.
    public SymbolTable previousTable;

    /**
     * Creates a new symbol table with the given previous symbol table stored, and initializes
     * the new table.
     *
     * @param table The symbol table of the outer scope of this table.
     */
    public SymbolTable(SymbolTable table) {
        this.table = new Hashtable<>();
        previousTable = table;
    }

    /**
     * Store a declared symbol in this table.
     *
     * @param declNode The declaration node containing the type and identifier.
     */
    public void storeSymbol(DeclNode declNode) {
        Symbol s = new Symbol(declNode.type, declNode.id);
        table.put(declNode.id.id, s);
    }

    /**
     * Get the symbol for the given token.
     *
     * If it is not found it the current, it will continue to look up
     * the symbol in a chain of higher scope symbol tables.
     *
     * @param token The token containing the identifier to look up.
     * @return The symbol for the given token, or null if it is not found.
     */
    public Symbol getSymbol(Token token) {
        Word w = (Word) token;
        for (SymbolTable s = this; s != null; s = s.previousTable) {
            Symbol found = s.table.get(w.lexeme);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
