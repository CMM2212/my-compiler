package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.ExpressionNode;
import compiler.parser.ast.nodes.LineTrackingNode;
import compiler.parser.ast.nodes.StatementNode;
import compiler.parser.ast.nodes.expressions.LocNode;

public class AssignmentNode implements StatementNode, LineTrackingNode {
    public LocNode left;
    public ExpressionNode expression;

    public AssignmentNode() {}

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
