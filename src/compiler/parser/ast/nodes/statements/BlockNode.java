package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.StatementNode;
import compiler.parser.ast.nodes.declarations.DeclNode;
import compiler.symbols.Env;

import java.util.ArrayList;
import java.util.List;

public class BlockNode implements StatementNode {
    public List<DeclNode> decls;
    public List<StatementNode> statements;

    public BlockNode parent;
    public Env table;

    public BlockNode(BlockNode parent) {
        this.parent = parent;
        this.decls = new ArrayList<>();
        this.statements = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}