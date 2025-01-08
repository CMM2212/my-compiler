package compiler.parser.ast.nodes.declarations;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.DeclarationNode;
import compiler.parser.ast.nodes.terminals.BasicNode;
import compiler.parser.ast.nodes.terminals.NumNode;

public class TypeNode implements DeclarationNode {
    public BasicNode type;
    public ArrayTypeNode array = null;

    public TypeNode() {
    }

    public int getDepth() {
        ArrayTypeNode current = array;
        int depth = 0;
        while (current != null) {
            depth++;
            current = current.type;
        }
        return depth;
    }

    public NumNode getDimSize(int dim) {
        ArrayTypeNode current = array;
        for (int i = 0; i < dim; i++) {
            current = current.type;
        }
        return current.size;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }

    public String toString() {
        String arrayString = "";
        for (int i = 0; i < getDepth(); i++) {
            arrayString += "[]";
        }
        return type.type.toString() + arrayString;
    }
}
