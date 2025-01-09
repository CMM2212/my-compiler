package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.StatementNode;
import compiler.parser.ast.nodes.declarations.DeclNode;
import compiler.symbols.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class BlockNode implements StatementNode {
    public List<DeclNode> decls = new ArrayList<>();
    public List<StatementNode> statements = new ArrayList<>();

    public SymbolTable table;

    public BlockNode() {
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}