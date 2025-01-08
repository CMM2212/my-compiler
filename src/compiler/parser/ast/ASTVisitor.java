package compiler.parser.ast;

import compiler.parser.ast.nodes.declarations.ArrayTypeNode;
import compiler.parser.ast.nodes.declarations.DeclNode;
import compiler.parser.ast.nodes.declarations.TypeNode;
import compiler.parser.ast.nodes.expressions.ArrayLocNode;
import compiler.parser.ast.nodes.expressions.LocNode;
import compiler.parser.ast.nodes.expressions.operations.BinaryExpressionNode;
import compiler.parser.ast.nodes.expressions.operations.UnaryNode;
import compiler.parser.ast.nodes.statements.*;
import compiler.parser.ast.nodes.structures.ProgramNode;
import compiler.parser.ast.nodes.terminals.*;

public interface ASTVisitor {
    default void visit(ProgramNode n){};
    default void visit(BlockNode n){};
    default void visit(DeclNode n){};
    default void visit(TypeNode n){};
    default void visit(ArrayTypeNode n){};

    default void visit(AssignmentNode n){};
    default void visit(IfNode n){};
    default void visit(WhileNode n){};
    default void visit(DoWhileNode n){};
    default void visit(IfFalseNode n){};
    default void visit(IfTrueNode n){};

    default void visit(LocNode n){};
    default void visit(ArrayLocNode n){};

    default void visit(BinaryExpressionNode n){};
    default void visit(UnaryNode n){};
    default void visit(ParenthesisNode n){};

    default void visit(BasicNode n){};
    default void visit(BreakNode n){};
    default void visit(FalseNode n){};
    default void visit(IdNode n){};
    default void visit(NumNode n){};
    default void visit(RealNode n){};
    default void visit(TrueNode n){};
    default void visit(GotoNode n){};
    default void visit(LabelNode n){};
    default void visit(TempNode n){};
}
