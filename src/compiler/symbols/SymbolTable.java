package compiler.symbols;

import compiler.lexer.tokens.Token;
import compiler.lexer.tokens.Word;
import compiler.parser.ast.nodes.declarations.DeclNode;

import java.util.Hashtable;

public class SymbolTable {
    Hashtable<String, Symbol> table;
    public SymbolTable prev;

    public SymbolTable(SymbolTable n) {
        table = new Hashtable<>();
        prev = n;
    }

    public void storeSymbol(DeclNode n) {
        Symbol s = new Symbol(n.type, n.id);
        table.put(n.id.id, s);
    }

    public Symbol getSymbol(Token token) {
        Word w = (Word) token;
        for (SymbolTable s = this; s != null; s = s.prev) {
            Symbol found = s.table.get(w.lexeme);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
