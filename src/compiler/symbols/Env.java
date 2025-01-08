package compiler.symbols;

import compiler.lexer.tokens.Word;

import java.util.Hashtable;

public class Env {
    Hashtable<String, Symbol> table;
    public Env prev;

    public Env(Env n) {
        table = new Hashtable<>();
        prev = n;
    }

    public void put(Word w, Symbol symbol) {
        table.put(w.lexeme, symbol);
    }

    public Symbol get(Word w) {
        for( Env e = this; e != null; e = e.prev ) {
            Symbol found = (e.table.get(w.lexeme));
            if( found != null )
                return found;
        }
        return null;
    }
}
