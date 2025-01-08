package compiler.parser.ast.nodes;

import compiler.parser.ast.ASTVisitor;

public interface Node {
    void accept(ASTVisitor v);
}
