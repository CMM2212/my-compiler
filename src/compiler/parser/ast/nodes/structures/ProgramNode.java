package compiler.parser.ast.nodes.structures;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.Node;
import compiler.parser.ast.nodes.statements.BlockNode;

public class ProgramNode implements Node {
    public BlockNode block;

    public ProgramNode() {}

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
