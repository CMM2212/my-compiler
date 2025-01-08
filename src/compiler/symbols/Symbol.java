package compiler.symbols;

import compiler.lexer.tokens.Word;
import compiler.parser.ast.nodes.declarations.TypeNode;
import compiler.parser.ast.nodes.terminals.IdNode;

public class Symbol {
    public String name;
    public Word token;
    public TypeNode type;
    public IdNode id;

    public Symbol(String n, Word t, TypeNode tn, IdNode i) {
        name = n;
        token = t;
        type = tn;
        id = i;
    }
}
