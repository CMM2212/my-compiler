package compiler.parser.ast.nodes.terminals;

import compiler.lexer.tokens.Type;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.TerminalNode;

public class BasicNode implements TerminalNode {
    public Type type;

    public BasicNode() {}

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
