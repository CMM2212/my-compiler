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

import java.util.Set;

/**
 * A type checker responsible for validating types in the AST.
 *
 * The type checker specifically handles the following:
 * - Assignment of expressions to variables
 *    - Ensures the types match
 *    - Ensures arrays are not allowed in assignment
 *    - Ensures arrays are accessed with the correct number of dimensions
 *    - Allows int to float assignment
 * - Binary expressions
 *    - Ensures logical operators have boolean operands
 *    - Ensures comparison operators have matching operands
 *    - Ensures arithmetic operators have numeric operands
 * - Unary Expressions
 *    - Ensures '!' operator has boolean operand
 *    - Ensures '-' operator has numeric operand
 *
 * The type checker works by recursively calling visit on each node until it reaches a terminal node
 * which the type can be known of, then it stores that type within the node, and then its parent node sets
 * its type to this child node that was visited. And this continues upwards until all expression nodes have
 * a type set. This is what allows the type checker to be able to compare compound expressions.
 *
 * For example: ((3 + 4.5) > 3)
 *  - It first visits 3 and sets its type to int
 *  - It then visits 4.5 and sets its type to float
 *  - Then in the binary expression node, it sets the type of (3 + 4.5) to float.
 *  - Then it visits 3 and sets its type to int
 *  - Then it does the comparison binary expression and sets the result to boolean.
 *  - The final type of the parentheses node is boolean then in the end.
 *
 */
public class TypeChecker implements ASTVisitor {
    public ProgramNode program;
    private SymbolTable currentSymbolTable;

    // Sets for checking operators
    private static final Set<String> LOGIC_OPERATORS = Set.of("&&", "||");
    private static final Set<String> COMPARISON_OPERATORS = Set.of("==", "!=", "<", "<=", ">", ">=");
    private static final Set<String> ARITHMETIC_OPERATORS = Set.of("+", "-", "*", "/");

    /**
     * Creates a TypeChecker and immediately visits the program node from the given parser instance.
     *
     * This will traverse the AST, resolving the types, and validating them.
     *
     * @param parser The parser containing a parsed AST.
     */
    public TypeChecker(Parser parser) {
        program = parser.program;
        visit(program);
    }

    // Helper Methods

    /**
     * Checks if it is the case of an int being assigned to a float.
     *
     * This is valid within the language because there is no loss of information.
     *
     * @param left Left side of the assignment.
     * @param right Right side of the assignment.
     * @return True if left is float and right is int, false otherwise.
     */
    public static Boolean isIntToFloatAssignment(TypeNode left, TypeNode right) {
        return left.type == Type.Float && right.type == Type.Int;
    }

    /**
     * Checks if the given string is a logical operator. (&&, ||)
     *
     * @param operator The operator to check.
     * @return True if the operator is a logical operator, false otherwise.
     */
    private static Boolean isLogicOperator(String operator) {
        return LOGIC_OPERATORS.contains(operator);
    }

    /**
     * Checks if the given string is a comparison operator. (==, !=, <, <=, >, >=)
     *
     * @param operator The operator to check.
     * @return True if the operator is a comparison operator, false otherwise.
     */
    private static Boolean isComparisonOperator(String operator) {
        return COMPARISON_OPERATORS.contains(operator);
    }

    /**
     * Checks if the given string is an arithmetic operator. (+, -, *, /)
     *
     * @param operator The operator to check.
     * @return True if the operator is an arithmetic operator, false otherwise.
     */
    private static Boolean isArithmeticOperator(String operator) {
        return ARITHMETIC_OPERATORS.contains(operator);
    }

    /**
     * Resolves the resulting type of a binary operation.
     *
     * If either side of the binary expression is a float, then the resulting type
     * should be a float. Otherwise, the resulting type should be the type of the left side
     * because the types should be the same (after having been validated through other methods).
     *
     * @param left The type of the left side.
     * @param right The type of the right side.
     * @return The resulting type of the operation.
     */
    private static Type resolveResultingType(TypeNode left, TypeNode right) {
        if (left.type == Type.Float || right.type == Type.Float)
            return Type.Float;
        return left.type;
    }

    // Validation Methods

    /**
     * Validates that neither the left or right are arrays.
     *
     * Arrays cannot be used for assignment in this language.
     *
     * @param left Type of the left side of the assignment.
     * @param right Type of the right side of the assignment.
     * @param line Line number from source code.
     * @throws TypeException If either side is an array.
     */
    private static void validateNotArrayAssignment(TypeNode left, TypeNode right, int line) {
        if (left.isArray() || right.isArray())
            throw new TypeException(
                    """
                    cannot assign arrays: attempted to assign '%s' to '%s'
                    """.formatted(left, right),
                    line);
    }

    /**
     * Validates that the left and right are both the same type.
     *
     * @param left Type of the left side of the assignment.
     * @param right Type of the right side of the assignment.
     * @param line Line number from source code.
     * @throws TypeException If the types do not match.
     */
    private static void validateTypesMatchAssignment(TypeNode left, TypeNode right, int line) {
        if (left.type != right.type)
            throw new TypeException(
                    """
                    type mismatch: cannot assign '%s' to '%s'
                    """.formatted(right, left),
                    line);
    }

    /**
     * Validates that the array is accessed with its exact declared amount of dimensions.
     *
     * All arrays should be accessed down to a single value. For example, if this is declared "int[5][5][5] x" then
     * it must be accessed with 3 dimensions too: x[1][2][3]. x[1], x[1][2][3][4], or x would all be invalid.
     *
     * @param node The LocNode being accessed.
     * @param declaredType The declared type of the LocNode's identifier.
     * @throws TypeException If an array is accessed with the wrong number of dimensions.
     */
    private static void validateArrayAccess(LocNode node, TypeNode declaredType) {
        int declaredDepth = declaredType.getDepth();
        int accessedDepth = node.getDepth();

        // Check for both cases to give a more descriptive error message.
        // If both of these are false, then the array was accessed correctly.
        if (declaredDepth < accessedDepth)
            throw new TypeException(
                    """
                    '%s' cannot be accessed as a %d dimensional array; it is only a %d dimensional array
                    """.formatted(node.id.word, accessedDepth, declaredDepth),
                    node.getLine());
        else if (declaredDepth > accessedDepth)
            throw new TypeException(
                    """
                    '%s' cannot be accessed as a %d dimensional array; it is a %d dimensional array
                    """.formatted(node.id.word, accessedDepth, declaredDepth),
                    node.getLine());
    }

    /**
     * Validates both operands in a logical operation are boolean.
     *
     * The binary expression node is passed so that the error message can access the operator
     * and line.
     *
     * @param left The type of the left operand.
     * @param right The type of the right operand.
     * @param node The BinaryExpressionNode being validated.
     * @throws TypeException If the operands are not boolean for a logical operator.
     */
    private static void validateLogicOperator(TypeNode left, TypeNode right, BinaryExpressionNode node) {
        if (left.type != Type.Bool || right.type != Type.Bool)
            throw new TypeException(
                    """
                    logical operator '%s' expects boolean types, not '%s' and '%s'
                    """.formatted(node.operator, left, right),
                    node.getLine());
    }

    /**
     * Validates both operands in a comparison operation are the same type.
     *
     * @param left The type of the left operand.
     * @param right The type of the right operand.
     * @param node The BinaryExpressionNode being validated.
     * @throws TypeException If the operands are not the same type for a comparison operator.
     */
    private static void validateComparisonOperator(TypeNode left, TypeNode right, BinaryExpressionNode node) {
        if (left.type != right.type)
            throw new TypeException(
                    """
                    comparison operator '%s' expects same types, not '%s' and '%s'
                    """.formatted(node.operator, left, right),
                    node.getLine());
        node.setType(Type.Bool);
    }

    /**
     * Validates both operands in an arithmetic operation are numeric.
     *
     * @param left The type of the left operand.
     * @param right The type of the right operand.
     * @param node The BinaryExpressionNode being validated.
     * @throws TypeException If the operands are not both numeric for an arithmetic operator.
     */
    private static void validateArithmeticOperator(TypeNode left, TypeNode right, BinaryExpressionNode node) {
        if (!Type.isNumeric(left.type) || !Type.isNumeric(right.type))
            throw new TypeException(
                    """
                    arithmetic operator '%s' expects numeric types, not '%s' and '%s'
                    """.formatted(node.operator, left, right),
                    node.getLine());
    }

    /**
     * Validate that the expression is boolean for a '!' operator.
     *
     * @param node The UnaryNode to validate.
     * @throws TypeException If the expression is not boolean for a '!' operator.
     */
    private static void validateNotOperator(UnaryNode node) {
        if (node.getType().type != Type.Bool)
            throw new TypeException(
                    """
                    '!' operator expects boolean type, not '%s'
                    """.formatted(node.getType()),
                    node.getLine());
    }

    /**
     * Validate that the expression is numeric for a '-' operator.
     *
     * @param node The UnaryNode to validate.
     * @throws TypeException If the expression is not numeric for a '-' operator.
     */
    private static void validateNegationOperator(UnaryNode node) {
        if (!Type.isNumeric(node.getType().type))
            throw new TypeException(
                    """
                    unary '-' operator expects numeric type, not '%s'
                    """.formatted(node.getType()),
                    node.getLine());
    }

    // Visit methods

    /**
     * Visits a ProgramNode, the entry point of the AST, and immediately visits its block.
     *
     * @param node The ProgramNode to visit.
     */
    @Override
    public void visit(ProgramNode node) {
        node.block.accept(this);
    }

    /**
     * Visits a BlockNode, checking the types of each statement within it.
     *
     * The declarations can be ignored for the type checker, so the only things visited
     * are the statements. Each are visited to ensure they are valid.
     *
     * The symbol table is retrieved from the BlockNode and used so that the visited statements
     * can access their correct symbol table they were created in.
     *
     * @param node The BlockNode to visit.
     */
    @Override
    public void visit(BlockNode node) {
        currentSymbolTable = node.table;
        for (StatementNode stmt : node.statements)
            stmt.accept(this);
        currentSymbolTable = currentSymbolTable.previousTable;
    }

    /**
     * Visits an assignment node, checking the types are compatible.
     *
     * The left LocNode and the right ExpressionNode are visited to get their types and then
     * these two types are validated to ensure the expression can be assigned to the left.
     *
     * @param node The AssignmentNode to visit.
     * @throws TypeException If the types are incompatible for assignment.
     */
    @Override
    public void visit(AssignmentNode node) {
        node.left.accept(this);
        node.expression.accept(this);

        TypeNode leftType = node.left.getType();
        TypeNode rightType = node.expression.getType();

        validateNotArrayAssignment(leftType, rightType, node.getLine());

        if (isIntToFloatAssignment(leftType, rightType))
            return;

        validateTypesMatchAssignment(leftType, rightType, node.getLine());
    }

    /**
     * Visit an IfNode, visiting its body and expression and optional else statement to
     * ensure they are valid.
     *
     * @param node The IfNode to visit.
     */
    @Override
    public void visit(IfNode node) {
        node.expression.accept(this);
        node.thenStatement.accept(this);
        if (node.elseStatement != null) {
            node.elseStatement.accept(this);
        }
    }

    /**
     * Visit a WhileNode, visiting its body and expression to ensure they are valid.
     *
     * @param node The WhileNode to visit.
     */
    @Override
    public void visit(WhileNode node) {
        node.expression.accept(this);
        node.body.accept(this);
    }

    /**
     * Visit a DoWhileNode, visiting its body and expression to ensure they are valid.
     *
     * @param node The DoWhileNode to visit.
     */
    @Override
    public void visit(DoWhileNode node) {
        node.body.accept(this);
        node.expression.accept(this);
    }

    /**
     * Visit a LocNode, setting its type to the type of its id and validating that the array access is valid.
     *
     * Ensures that the array is accessed with the correct number of dimensions and valid indices.
     *
     * @param node The LocNode to visit.
     * @throws TypeException If the array access is invalid.
     */
    @Override
    public void visit(LocNode node) {
        node.id.accept(this);
        TypeNode declaredType = node.id.getType();

        validateArrayAccess(node, declaredType);

        node.setType(declaredType.type);
    }

    /**
     * Visit a binary node, setting its type to the type of its expression and validating that the
     * operands match the operator.
     *
     * Ensures that logical operators like && and || have boolean operands, comparison operators like
     * == and != have matching operands, and arithmetic operators like +, -, *, and / have numeric operands.
     *
     * The method also makes sure that the resulting type of logic/comparison operators is boolean, and the
     * resulting type of arithmetic operators is resolved as a float or int.
     *
     * @param node The BinaryExpressionNode to visit.
     * @throws TypeException If the operands do not match the operator.
     */
    @Override
    public void visit(BinaryExpressionNode node) {
        node.left.accept(this);
        node.right.accept(this);

        TypeNode left = node.left.getType();
        TypeNode right = node.right.getType();



        if (isLogicOperator(node.operator)) {
            validateLogicOperator(left, right, node);
            node.setType(Type.Bool);
        }
        else if (isComparisonOperator(node.operator)) {
            validateComparisonOperator(left, right, node);
            node.setType(Type.Bool);
        }
        else if (isArithmeticOperator(node.operator)) {
            validateArithmeticOperator(left, right, node);
            node.setType(resolveResultingType(left, right));
        }
    }

    /**
     * Visit a unary node, setting its type to the type of its expression and validating
     * that the correct types are being used for the operator.
     *
     * @param node The UnaryNode to visit.
     * @throws TypeException If a non-numeric is used with '-' or a non-boolean is used with '!'
     */
    @Override
    public void visit(UnaryNode node) {
        node.expression.accept(this);
        node.setType(node.expression);

        // Ensure the correct types are being used for the operator.
        if (node.operator.tag == Tag.NOT)
            validateNotOperator(node);
        else if (node.operator.tag == Tag.SUB)
            validateNegationOperator(node);
    }

    /**
     * Visit a ParenthesisNode, setting its type to the type of its expression.
     *
     * @param node The ParenthesisNode to visit.
     */
    @Override
    public void visit(ParenthesisNode node) {
        node.expression.accept(this);
        node.setType(node.expression);
    }


    /**
     * Visit an IdNode, retrieving its type from the symbol table.
     *
     * @param node The IdNode to visit.
     */
    @Override
    public void visit(IdNode node) {
        node.setType(currentSymbolTable.getSymbol(node.word));
    }

    /**
     * Visit a NumNode, setting its type to int.
     *
     * @param node The NumNode to visit.
     */
    @Override
    public void visit(NumNode node) {
        node.setType(Type.Int);
    }

    /**
     * Visit a RealNode, setting its type to float.
     *
     * @param node The RealNode to visit.
     */
    @Override
    public void visit(RealNode node) {
        node.setType(Type.Float);
    }

    /**
     * Visit a FalseNode, setting its type to boolean.
     *
     * @param node The FalseNode to visit.
     */
    @Override
    public void visit(FalseNode node) {
        node.setType(Type.Bool);
    }

    /**
     * Visit a TrueNode, setting its type to boolean.
     *
     * @param node The TrueNode to visit.
     */
    @Override
    public void visit(TrueNode node) {
        node.setType(Type.Bool);
    }
}
