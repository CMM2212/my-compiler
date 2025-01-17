package compiler.parser;

import compiler.errors.SyntaxException;
import compiler.lexer.Lexer;
import compiler.lexer.Tag;
import compiler.lexer.tokens.*;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.*;
import compiler.parser.ast.nodes.declarations.*;
import compiler.parser.ast.nodes.expressions.*;
import compiler.parser.ast.nodes.expressions.operations.BinaryExpressionNode;
import compiler.parser.ast.nodes.expressions.operations.UnaryNode;
import compiler.parser.ast.nodes.statements.*;
import compiler.parser.ast.nodes.structures.ProgramNode;
import compiler.parser.ast.nodes.terminals.*;
import compiler.symbols.SymbolTable;
import compiler.symbols.Symbol;

import java.util.Set;

public class Parser implements ASTVisitor {
    public ProgramNode program;
    public SymbolTable outerSymbolTable = null; // Current symbol table.
    public Lexer lexer;
    public Token look = null;
    private int loopCount = 0; // Keeps track of loops to validate break statements.

    // All binary operators
    private final Set<Integer> BINARY_OPERATOR_TAGS = Set.of(Tag.MUL, Tag.DIV, Tag.ADD, Tag.SUB, Tag.LE, Tag.LT, Tag.GE,
            Tag.GT, Tag.EQ, Tag.NE, Tag.AND, Tag.OR);

    /***
     * Creates a lexer class and initializes the parsing process immediately.
     *
     * @param lexer The lexer class to retrieve tokens from.
     */
    public Parser(Lexer lexer) {
        this.lexer = lexer;
        program = new ProgramNode();
        move();
        visit(program);
    }

    /**
     * Move to the next token.
     */
    void move() {
        look = lexer.getNextToken();
    }

    /**
     * Check the expected tag matches the received one.
     *
     * The tag passed in should be the expected tag of the current look token. This
     * is to confirm that what you store in the node is the expected tag type. For example
     * if you pass in match(Tag.LBRACE) then it will check the current token is in fact an '}' token.
     *
     * If it doesn't match, it will raise a syntax exception and pass a string with more details. Specific
     * scenarios for if it is an unexpected end of file or a missing semicolon are handled as well.
     *
     * @param tag The expected tag of the current token.
     */
    void match(int tag) {
        if (look.tag == tag)
            move();
        else if (look.tag == Tag.EOF)
            throw new SyntaxException("unexpected end of file");
        else if (tag == Tag.SEMICOLON) {
            // For printing the error, it's important the lexer knows that it might need
            // to return to the previous line to point out where the ';' was expected to be.
            lexer.missingSemicolon = true;
            throw new SyntaxException("expected ';' at end of statement");
        } else
            throw new SyntaxException("expected '" + lexer.convertTagToString(tag) + "' instead of '" + look + "'");
    }

    /**
     * Checks that the given tag is a binary operator.
     *
     * @param op The tag to check.
     * @return true if the tag is a binary operator, false otherwise.
     */
    boolean isBinaryOperation(int op) {
        return BINARY_OPERATOR_TAGS.contains(op);
    }

    /**
     * Gets the precedence of the given operator.
     *
     * This is used to determine the order of operations in binary expressions where
     * higher precedence operators like multiplication and division are given higher
     * values then lower precedence ones like addition and subtraction.
     *
     * @param op The binary operator to get the precedence of.
     * @return The precedence of the operator.
     */
    int getPrecedence(int op) {
        return switch (op) {
            case Tag.MUL, Tag.DIV -> 12;
            case Tag.ADD, Tag.SUB -> 11;
            case Tag.LE, Tag.LT, Tag.GE, Tag.GT -> 9;
            case Tag.EQ, Tag.NE -> 8;
            case Tag.AND -> 7;
            case Tag.OR -> 6;
            default -> -1;
        };
    }


    /**
     * Visit the root node of the program which contains a single block.
     *
     * Grammar Rule:
     * program -> block
     *
     * @param node The program node to visit.
     */
    @Override
    public void visit(ProgramNode node) {
        node.block = new BlockNode();
        node.block.accept(this);
    }

    /**
     * Visit a block node representing a block of code with its own scope.
     *
     * This parse the declarations and statements of a block. It will also
     * store the previous symbol table and create a new one for this block.
     *
     * Grammar Rule:
     * block -> '{' decls statements '}'
     *
     * @param node The block node to visit.
     */
    @Override
    public void visit(BlockNode node) {
        match(Tag.LBRACE);
        // Save current symbol table, and create a new one for this block.
        outerSymbolTable = new SymbolTable(outerSymbolTable);
        node.table = outerSymbolTable;

        // So long as next token is a type, parse declarations.
        while (look.tag == Tag.BASIC) {
            DeclNode decl = new DeclNode();
            decl.accept(this);
            node.decls.add(decl);
        }

        // Parse statements until end of block.
        while (look.tag != Tag.RBRACE) {
            StatementNode statementNode = parseStatement();
            node.statements.add(statementNode);
        }
        outerSymbolTable = outerSymbolTable.previousTable;
        match(Tag.RBRACE);
    }

    /**
     * Visit a declaration node representing a variable declaration.
     *
     * This method will parse the type, including array dimensions, and the identifier
     * and store this information in the symbol table for the current block.
     *
     * Grammar Rule:
     * decl -> type id ';'
     *
     * @param node The declaration node to visit.
     */
    @Override
    public void visit(DeclNode node) {
        node.type = new TypeNode();
        node.type.accept(this);

        node.id = new IdNode();
        node.id.accept(this);

        // Store declared symbol in symbol table.
        outerSymbolTable.storeSymbol(node);

        match(Tag.SEMICOLON);
    }

    /**
     * Visit a type node representing a basic type and optional array dimensions.
     *
     * This method will parse the basic type (int or float) and parse its dimensions
     * if it is an array. This is called during the declarations.
     *
     * type -> 'int' [array] | 'float' [array]
     *
     * @param node The type node to visit.
     */
    @Override
    public void visit(TypeNode node) {
        node.type = (Type) look;
        match(Tag.BASIC);

        if (look.tag == Tag.LBRACKET) {
            node.array = new ArrayTypeNode();
            node.array.accept(this);
        }
    }

    /**
     * Visit an array of a type node representing an array of a basic type.
     *
     * This method will create a recursive array type node which each additional dimension of the
     * array is stored as an additional ArrayTypeNode. Each dimension also will have its size, an integer,
     * parsed and stored.
     *
     * Grammar Rule:
     * array -> '[' num ']' [array]
     *
     * @param node Array type node to visit.
     * @throws SyntaxException If the array size is not an integer.
     */
    @Override
    public void visit(ArrayTypeNode node) {
        match(Tag.LBRACKET);
        if (look.tag != Tag.NUM)
            throw new SyntaxException("expected integer array size instead of '" + look + "'");
        node.size = new NumNode();
        node.size.accept(this);
        match(Tag.RBRACKET);

        if (look.tag == Tag.LBRACKET) {
            node.type = new ArrayTypeNode();
            node.type.accept(this);
        }
    }

    /**
     * Creates a new statement node based on the next token and parses it.
     *
     * This method will create a new statement node based on the next token. For example if the next token is
     * an identifier then the only valid possibility is that it's the beginning of a statement node. Once
     * the new statement node is created its visited as well to be parsed.
     *
     * @return The parsed statement node.
     * @throws SyntaxException If the next token is not a valid start of a statement.
     */
    public StatementNode parseStatement() {
        StatementNode n = switch (look.tag) {
            case Tag.ID -> new AssignmentNode();
            case Tag.IF -> new IfNode();
            case Tag.WHILE -> new WhileNode();
            case Tag.DO -> new DoWhileNode();
            case Tag.BREAK -> new BreakNode();
            case Tag.LBRACE -> new BlockNode();
            case Tag.EOF -> throw new SyntaxException("unexpected end of file; did you miss a closing brace?");
            default ->
                    throw new SyntaxException("invalid start of a statement '" + lexer.convertTagToString(look.tag) + "'");
        };
        n.accept(this);
        return n;
    }

    /**
     * Visit an assignment node representing an assignment of an identifier/array to an expression.
     *
     * This method will parse the left side of the assignment which is a LocNode (identifier/array), and then
     * parse the expression on the right side of the assignment.
     *
     * Grammar Rule:
     * assignment -> loc '=' expression ';'
     *
     * @param node The assignment node to visit.
     */
    @Override
    public void visit(AssignmentNode node) {
        node.setLine(lexer.getCurrentLine()); // Used for error messages.

        node.left = new LocNode();
        node.left.accept(this);

        match(Tag.ASSIGN);
        node.expression = parseExpression();
        match(Tag.SEMICOLON);
    }

    /**
     * Visit an if node representing an if statement with an optional else statement.
     *
     * This method will parse the expression inside the if statement and its corresponding then statement.
     * If there is an else tag after the then statement, it will additionally parse and store that.
     *
     * Grammar Rule:
     * if -> 'if' '(' expression ')' statement ['else' statement]
     *
     * @param node The if node to visit.
     */
    @Override
    public void visit(IfNode node) {
        match(Tag.IF);
        match(Tag.LPAREN);
        node.expression = parseExpression();
        match(Tag.RPAREN);

        node.thenStatement = parseStatement();

        if (look.tag == Tag.ELSE) {
            match(Tag.ELSE);
            node.elseStatement = parseStatement();
        }
    }

    /**
     * Visit a while node representing a while loop.
     *
     * This method will parse the expression inside the while loop and its corresponding body statement.
     *
     * while -> 'while' '(' expression ')' statement
     *
     * @param node The while node to visit.
     */
    @Override
    public void visit(WhileNode node) {
        match(Tag.WHILE);
        match(Tag.LPAREN);
        node.expression = parseExpression();
        match(Tag.RPAREN);

        // loopCount is needed to validate break statements.
        loopCount++;
        node.body = parseStatement();
        loopCount--;
    }

    /**
     * Visit a do while node representing a do while loop.
     *
     * This method will parse the body of the loop and the expression that is checked at the end of the loop.
     *
     * Grammar Rule:
     * do -> 'do' statement 'while' '(' expression ')' ';'
     *
     * @param node The do while node to visit.
     */
    @Override
    public void visit(DoWhileNode node) {
        match(Tag.DO);
        // loopCount is needed to validate break statements.
        loopCount++;
        node.body = parseStatement();
        loopCount--;

        match(Tag.WHILE);
        match(Tag.LPAREN);
        node.expression = parseExpression();
        match(Tag.RPAREN);
        match(Tag.SEMICOLON);
    }


    /**
     * Visit a loc node representing an identifier or an array.
     *
     * This method will attempt to retrieve an identifier from the symbol table, and reuse that
     * IdNode if it exists. If it can't be found then it will raise a syntax exception saying
     * it is not declared.
     *
     * If the identifier is followed by a '[' then it will try to parse the array indexes as well.
     *
     * Grammar Rule:
     * loc -> id [array]
     *
     * @param node The loc node to visit.
     * @throws SyntaxException If the identifier is not declared.
     */
    @Override
    public void visit(LocNode node) {
        // Ensure the original line is stored in case the id/array extends over multiple lines.
        int line = lexer.getCurrentLine();
        Symbol symbol = outerSymbolTable.getSymbol(look);
        if (symbol == null)
            throw new SyntaxException("'" + look + "' is not declared");
        // Use previously created IdNode from symbol table.
        node.id = symbol.id;
        move();

        if (look.tag == Tag.LBRACKET) {
            node.array = new ArrayLocNode();
            node.array.accept(this);
        }
        node.setLine(line);
    }

    /**
     * Visit an array loc node representing accessing an array element of an identifier.
     *
     * This will continue to parse the array indexes as expressions until there are no more
     * '[' tokens. This represents accessing an array. For example, x[3+5][3] would involve parsing
     * the 3+5 and 3 as expressions here.
     *
     * array -> '[' expression ']' [array]
     *
     * @param node The array loc node to visit.
     */
    @Override
    public void visit(ArrayLocNode node) {
        int line = lexer.getCurrentLine();

        match(Tag.LBRACKET);
        node.expression = parseExpression();
        match(Tag.RBRACKET);

        if (look.tag == Tag.LBRACKET) {
            node.array = new ArrayLocNode();
            node.array.accept(this);
        }

        node.setLine(line);
    }

    /**
     * Parse an expression which can be a binary expression or a factor.
     *
     * This method will first parse a factor such as a number, identifier, or parenthesis. If the next token
     * is a binary operator then that means it must be part of a binary expression. So the left hand side will
     * be passed into parseBinaryExpression to continue parsing the expression. If it is not a binary operator,
     * then the factor can just b returned.
     *
     * For example, with the expression "3 + 5" the 3 would be parsed as a factor, then it would look at
     * the next token of '+' and know it's a binary expression so it'd call parseBinaryExpression with the 3
     * as the left hand side.
     *
     * @return The parsed expression node.
     */
    public ExpressionNode parseExpression() {
        ExpressionNode n = parseFactor();

        if (isBinaryOperation(look.tag))
            return parseBinaryExpression(n, 0);
        else
            return n;
    }

    /**
     * Parse a binary expression given the left hand side and the precedence of the operator.
     *
     * This method will recursively parse the binary expression so long as it is not a lower precedence.
     * Each additionally parsed expression will be nested inside the previous one to ensure the higher
     * precedence ones are always evaluated first in the tree.
     *
     * Each time a new one is parsed, the combination will be stored as a new binary expression node and
     * represented as the LHS, which will be the final node returned. The LHS returned in the end is the entire
     * tree parsed here, not simply the left hand side.
     *
     * The idea behind this algorithm is that higher precedence operators will always be nested deeper within the
     * tree so that they will be evaluated first.
     *
     * Example A: 2 * 3 + 4
     *         (+)
     *        /   \
     *      (*)    4
     *     /   \
     *    2    3
     *
     * Example B: 2 + 3 * 4
     *       (+)
     *      /  \
     *     2   (*)
     *        /  \
     *       3    4
     *
     * @param lhs The left hand side of the binary expression.
     * @param precedence The precedence of the current operator.
     * @return A binary expression node.
     */
    public ExpressionNode parseBinaryExpression(ExpressionNode lhs, int precedence) {
        // Continue to parse so long as the next operator is not lower precedence.
        while (isBinaryOperation(look.tag) && (getPrecedence(look.tag) >= precedence)) {
            // Get the operator and move onto the expression
            Token op = look;
            move();
            // Parse the expression on the right hand side.
            ExpressionNode rhs = parseExpression();

            // Now if the next operator is higher precedence than the current operator, then
            // it should be handled first, so we need to take the current RHS and nest it
            // inside the next operator.
            while (getPrecedence(look.tag) > getPrecedence(op.tag))
                rhs = parseBinaryExpression(rhs, getPrecedence(look.tag));

            // Create a new binary node that will be the returned value which is the current LHS and RHS
            // combined with the operator.
            lhs = new BinaryExpressionNode(lhs, rhs, op.toString());
            lhs.accept(this);
        }
        return lhs;
    }

    /**
     * Visit a binary expression node representing a binary operation on two expressions.
     *
     * The actual parsing is handled in parseBinaryExpression, this method is just used to set the line number
     * for error messages.
     *
     * @param node The binary expression node to visit.
     */
    @Override
    public void visit(BinaryExpressionNode node) {
        node.setLine(lexer.getCurrentLine());
    }

    /**
     * Visit a unary node representing a unary operation on an expression.
     *
     * A unary node will only be created if it already has seen that there is a unary operator, so
     * a unary node will always contain an operator and expression.
     *
     * Grammar Rule:
     * unary -> '-' factor | '!' factor
     * @param node The unary node to visit.
     */
    @Override
    public void visit(UnaryNode node) {
        int line = lexer.getCurrentLine();

        node.operator = look;
        match(look.tag);

        node.expression = parseFactor();

        node.setLine(line);
    }

    /**
     * Parse a single factor and return the expression node.
     *
     * This method is responsible for determining what type of factor the next expression is by looking
     * at the next token. For example if it begins with an identifier, it knows it must be a LocNode.
     *
     * Grammar Rule:
     * factor -> unary | parenthesis | num | real | loc | 'true' | 'false'
     *
     * @return The parsed expression node.
     * @throws SyntaxException If the next token is not a valid factor.
     */
    public ExpressionNode parseFactor() {
        ExpressionNode n = switch (look.tag) {
            case Tag.SUB, Tag.NOT -> new UnaryNode();
            case Tag.LPAREN -> new ParenthesisNode();
            case Tag.NUM -> new NumNode();
            case Tag.REAL -> new RealNode();
            case Tag.ID -> new LocNode();
            case Tag.TRUE -> new TrueNode();
            case Tag.FALSE -> new FalseNode();
            default -> throw new SyntaxException("expected factor instead of '" + look + "'");
        };
        n.accept(this);
        return n;
    }

    /**
     * Visit a parenthesis node representing an expression inside parenthesis.
     *
     * Grammar Rule:
     * parenthesis -> '(' expression ')'
     *
     * @param node The parenthesis node to visit.
     */
    @Override
    public void visit(ParenthesisNode node) {
        match(Tag.LPAREN);
        node.expression = parseExpression();
        match(Tag.RPAREN);
    }

    /**
     * Visit a break node representing a break statement.
     *
     * Grammar Rule:
     * break -> 'break' ';'
     *
     * @param node The break node to visit.
     * @throws SyntaxException If the break statement is not within a loop.
     */
    @Override
    public void visit(BreakNode node) {
        // If it is not within a while/do-while loop, it is invalid
        if (loopCount == 0)
            throw new SyntaxException("'break' outside of loop");
        match(Tag.BREAK);
        match(Tag.SEMICOLON);
    }

    /**
     * Visit an id node representing an identifier.
     *
     * Grammar Rule:
     * id -> word
     *
     * @param node The id node to visit.
     */
    @Override
    public void visit(IdNode node) {
        node.id = look.toString();
        node.word = (Word) look;
        match(Tag.ID);
    }

    /**
     * Visit a num node representing an integer number.
     *
     * Grammar Rule:
     * num -> integer number
     *
     * @param node The num node to visit.
     */
    @Override
    public void visit(NumNode node) {
        node.num = ((Num) look).value;
        match(Tag.NUM);
    }

    /**
     * Visit a real node represent a floating point number.
     *
     * Grammar Rule:
     * real -> real number
     *
     * @param node The real node to visit.
     */
    @Override
    public void visit(RealNode node) {
        node.value = ((Real) look).value;
        match(Tag.REAL);
    }

    /**
     * Visit a false node representing the boolean literal false.
     *
     * Grammar Rule:
     * false -> 'false'
     *
     * @param node The false node to visit.
     */
    @Override
    public void visit(FalseNode node) {
        match(Tag.FALSE);
    }


    /**
     * Visit a true node representing the boolean literal true.
     *
     * Grammar Rule:
     * true -> 'true'
     *
     * @param node The true node to visit.
     */
    @Override
    public void visit(TrueNode node) {
        match(Tag.TRUE);
    }
}
