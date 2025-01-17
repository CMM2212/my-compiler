package compiler.parser.ast;

import compiler.parser.ast.nodes.declarations.ArrayTypeNode;
import compiler.parser.ast.nodes.declarations.DeclNode;
import compiler.parser.ast.nodes.declarations.TypeNode;
import compiler.parser.ast.nodes.expressions.ArrayLocNode;
import compiler.parser.ast.nodes.expressions.LocNode;
import compiler.parser.ast.nodes.expressions.ParenthesisNode;
import compiler.parser.ast.nodes.expressions.TempNode;
import compiler.parser.ast.nodes.expressions.operations.BinaryExpressionNode;
import compiler.parser.ast.nodes.expressions.operations.UnaryNode;
import compiler.parser.ast.nodes.statements.*;
import compiler.parser.ast.nodes.structures.ProgramNode;
import compiler.parser.ast.nodes.terminals.*;

/**
 * An interface for visitors to traverse the AST.
 *
 * Parser, type checker, and intermediate code generator, and intermediate code
 * printer all implement this interface to traverse the AST.
 *
 * This interface provides a method for each type of node in the AST. The goal
 * of the visitor pattern is to allow for processing of the AST without modifying
 * the nodes themselves, and it allows for double dispatch where the correct method
 * for the node type is called based on the type of the node passed to the visit method, and
 * the node passes this through its accept method.
 *
 * By default, each method is empty so that visitors can override only the
 * methods for the nodes they are interested in. For example, the parser visitor
 * does not need to override methods related to the intermediate code generator nodes
 * like TempNode or LabelNode.
 */
public interface ASTVisitor {
    default void visit(ProgramNode node){}
    default void visit(BlockNode node){}
    default void visit(DeclNode node){}
    default void visit(TypeNode node){}
    default void visit(ArrayTypeNode node){}

    default void visit(AssignmentNode node){}
    default void visit(IfNode node){}
    default void visit(WhileNode node){}
    default void visit(DoWhileNode node){}
    default void visit(IfFalseNode node){}
    default void visit(IfTrueNode node){}

    default void visit(LocNode node){}
    default void visit(ArrayLocNode node){}

    default void visit(BinaryExpressionNode node){}
    default void visit(UnaryNode node){}
    default void visit(ParenthesisNode node){}

    default void visit(BreakNode node){}
    default void visit(FalseNode node){}
    default void visit(IdNode node){}
    default void visit(NumNode node){}
    default void visit(RealNode node){}
    default void visit(TrueNode node){}
    default void visit(GotoNode node){}
    default void visit(LabelNode node){}
    default void visit(TempNode node){}
}
