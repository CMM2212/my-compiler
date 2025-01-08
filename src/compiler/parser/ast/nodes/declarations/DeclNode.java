package compiler.parser.ast.nodes.declarations;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.DeclarationNode;
import compiler.parser.ast.nodes.terminals.IdNode;

public class DeclNode implements DeclarationNode {
    public TypeNode type;
    public IdNode id;

    public DeclNode() {}

    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
