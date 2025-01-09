package compiler.typechecker;

import compiler.errors.*;
import compiler.lexer.Tag;
import compiler.lexer.tokens.*;
import compiler.parser.Parser;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.*;
import compiler.parser.ast.nodes.declarations.*;
import compiler.parser.ast.nodes.expressions.*;
import compiler.parser.ast.nodes.expressions.operations.*;
import compiler.parser.ast.nodes.statements.*;
import compiler.parser.ast.nodes.structures.ProgramNode;
import compiler.parser.ast.nodes.terminals.*;
import compiler.symbols.SymbolTable;
import compiler.symbols.Symbol;

import java.util.Set;

public class TypeChecker implements ASTVisitor {
    public Parser parser;
    public ProgramNode program;
    public SymbolTable env;

    private static final Set<String> LOGIC_OPERATORS = Set.of("&&", "||");
    private static final Set<String> COMPARISON_OPERATORS = Set.of("==", "!=", "<", "<=", ">", ">=");
    private static final Set<String> ARITHMETIC_OPERATORS = Set.of("+", "-", "*", "/");

    public TypeChecker(Parser parser) {
        this.parser = parser;
        program = parser.program;
        visit(program);
    }

    public static Boolean isIntToFloatAssignment(TypeNode left, TypeNode right) {
        return left.type == Type.Float && right.type == Type.Int;
    }

    private static Boolean isLogicOperator(String operator) {
        return LOGIC_OPERATORS.contains(operator);
    }

    private static Boolean isComparisonOperator(String operator) {
        return COMPARISON_OPERATORS.contains(operator);
    }

    private static Boolean isArithmeticOperator(String operator) {
        return ARITHMETIC_OPERATORS.contains(operator);
    }

    private static TypeNode resolveResultingType(TypeNode left, TypeNode right) {
        if (left.type == Type.Float || right.type == Type.Float)
            return new TypeNode(Type.Float);
        return new TypeNode(left.type);
    }

    private static void validateNotArrayAssignment(TypeNode left, TypeNode right, int line) {
        if (left.isArray() || right.isArray())
            throw new TypeException("cannot assign arrays" , line);
    }

    private static void validateTypesMatchAssignment(TypeNode left, TypeNode right, int line) {
        if (left.type != right.type)
            throw new TypeException("type mismatch: cannot assign '" + right + "' to '"
                    + left + "'", line);
    }

    private static void validateArrayAccess(LocNode n, TypeNode declaredType) {
        int declaredDepth = declaredType.getDepth();
        int accessedDepth = n.getDepth();

        if (declaredDepth < accessedDepth)
            throw new TypeException("'" + n.id.w + "' cannot be accessed as a " + accessedDepth +
                    " dimensional array; it is only a " + declaredDepth + " dimensional array", n.getLine());
        else if (declaredDepth > accessedDepth)
            throw new TypeException("cannot access '" + n.id.w + "' as an array; it is type '" +
                    declaredType + "'", n.getLine());
    }

    private static void validateLogicOperator(TypeNode left, TypeNode right, BinaryExpressionNode n) {
        if (left.type != Type.Bool || right.type != Type.Bool)
            throw new TypeException("logical operator '" + n.operator +
                    "' expects boolean types, not '" + left + "' and '" + right + "'", n.getLine());
    }

    private static void validateComparisonOperator(TypeNode left, TypeNode right, BinaryExpressionNode n) {
        if (left.type != right.type)
            throw new TypeException("comparison operator '" + n.operator +
                    "' expects same types, not '" + left + "' and '" + right + "'", n.getLine());
        n.setType(Type.Bool);
    }

    private static void validateArithmeticOperator(TypeNode left, TypeNode right, BinaryExpressionNode n) {
        if (!Type.numeric(left.type) || !Type.numeric(right.type))
            throw new TypeException("arithmetic operator '" + n.operator +
                    "' expects numeric types, not '" + left + "' and '" + right + "'", n.getLine());
    }

    private static void validateNotOperator(UnaryNode n) {
        if (n.getType().type != Type.Bool)
            throw new TypeException("'!' operator expects boolean type, not '" +
                    n.getType() + "'", n.getLine());
    }

    private static void validateNegationOperator(UnaryNode n) {
        if (!Type.numeric(n.getType().type))
            throw new TypeException("unary '-' operator expects numeric type, not '" +
                    n.getType() + "'", n.getLine());
    }

    @Override
    public void visit(ProgramNode n) {
        n.block.accept(this);
    }

    @Override
    public void visit(BlockNode n) {
        env = n.table;
        for (StatementNode stmt : n.statements)
            stmt.accept(this);
        env = env.prev;
    }

    @Override
    public void visit(AssignmentNode n) {
        n.left.accept(this);
        n.expression.accept(this);
        
        TypeNode leftType = n.left.getType();
        TypeNode rightType = n.expression.getType();

        validateNotArrayAssignment(leftType, rightType, n.getLine());

        if (isIntToFloatAssignment(leftType, rightType))
            return;

        validateTypesMatchAssignment(leftType, rightType, n.getLine());
    }

    @Override
    public void visit(IfNode n) {
        n.expression.accept(this);
        n.thenStatement.accept(this);
        if (n.elseStatement != null) {
            n.elseStatement.accept(this);
        }
    }

    @Override
    public void visit(WhileNode n) {
        n.expression.accept(this);
        n.body.accept(this);
    }

    @Override
    public void visit(DoWhileNode n) {
        n.body.accept(this);
        n.expression.accept(this);
    }

    @Override
    public void visit(LocNode n) {
        n.id.accept(this);
        TypeNode declaredType = n.id.getType();

        validateArrayAccess(n, declaredType);

        n.setType(declaredType.type);
    }

    @Override
    public void visit(BinaryExpressionNode n) {
        n.left.accept(this);
        n.right.accept(this);

        TypeNode left = n.left.getType();
        TypeNode right = n.right.getType();

        n.setType(resolveResultingType(left, right));

        if (isLogicOperator(n.operator))
            validateLogicOperator(left, right, n);
        else if (isComparisonOperator(n.operator))
            validateComparisonOperator(left, right, n);
        else if (isArithmeticOperator(n.operator))
            validateArithmeticOperator(left, right, n);
    }

    @Override
    public void visit(UnaryNode n) {
        n.right.accept(this);
        n.setType(n.right.getType());

        if (n.op.tag == Tag.NOT)
            validateNotOperator(n);
        else if (n.op.tag == Tag.SUB)
            validateNegationOperator(n);
    }

    @Override
    public void visit(ParenthesisNode n) {
        n.expression.accept(this);
        n.setType(n.expression.getType());
    }
    @Override
    public void visit(FalseNode n) {
        n.setType(Type.Bool);
    }

    @Override
    public void visit(IdNode n) {
        n.setType(env.getSymbol(n.w).type);
    }

    @Override
    public void visit(NumNode n) {
        n.setType(Type.Int);
    }

    @Override
    public void visit(RealNode n) {
        n.setType(Type.Float);
    }

    @Override
    public void visit(TrueNode n) {
        n.setType(Type.Bool);
    }
}
