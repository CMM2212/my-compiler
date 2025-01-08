package compiler.parser.ast.nodes.declarations;


import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.terminals.NumNode;

public class ArrayTypeNode extends TypeNode {
    public ArrayTypeNode type;
    public NumNode size;

    public ArrayTypeNode() {
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
