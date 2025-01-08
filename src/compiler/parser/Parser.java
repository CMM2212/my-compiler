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
import compiler.symbols.Env;
import compiler.symbols.Symbol;

import java.io.IOException;

public class Parser implements ASTVisitor {
    public ProgramNode program;
    public Env top = null;
    public Lexer lexer;
    public Token look = null;
    private int loopCount = 0;
    public BlockNode enclosingBlock = null;

    public Parser(Lexer l) {
        lexer = l;
        program = new ProgramNode();
        move();
        visit(program);
    }

    // Utility Methods
    void move() {
        try {
            look = lexer.scan();
        }
        catch(IOException e) {
            throw new SyntaxException(e.getMessage());
        }
    }

    void match(int t) {
        if (look.tag == t)
            move();
        else if (look.tag == Tag.EOF)
            throw new SyntaxException("unexpected end of file");
        else if (t == Tag.SEMICOLON) {
            lexer.missingSemicolon = true;
            throw new SyntaxException("expected ';' at end of statement");
        }
        else
            throw new SyntaxException("expected '" );//+ lexer.convertTagToString(t) + "' instead of '" + look + "'");
    }

    // General Nodes
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * program -> block
     */
    @Override
    public void visit(ProgramNode n) {
        n.block = new BlockNode(enclosingBlock);
        n.block.accept(this);
    }

    /**
     * block -> '{' decls statements '}'
     */
    @Override
    public void visit(BlockNode n) {
        match(Tag.LBRACE);
        // Save current symbol table, and create a new one for this block.
        top = new Env(top);
        n.table = top;
        enclosingBlock = n;

        // So long as next token is a type, parse declarations.
        while (look.tag == Tag.BASIC) {
            DeclNode decl = new DeclNode();
            decl.accept(this);
            n.decls.add(decl);
        }

        // Parse statements until end of block.
        while (look.tag != Tag.RBRACE) {
            StatementNode statementNode = parseStatement();
            n.statements.add(statementNode);
        }

        enclosingBlock = n.parent;
        top = top.prev;
        match(Tag.RBRACE);
    }

    /**
     * decl -> type id ';'
     */
    @Override
    public void visit(DeclNode n) {
        n.type = new TypeNode();
        n.type.accept(this);

        n.id = new IdNode();
        n.id.accept(this);

        // Store declared symbol in symbol table.
        top.put(n.id.w, new Symbol(n.id.id, n.id.w, n.type, n.id));

        match(Tag.SEMICOLON);
    }

    /**
     * type -> basic [array]
     */
    @Override
    public void visit(TypeNode n) {
        n.type = new BasicNode();
        n.type.accept(this);

        if (look.tag == Tag.LBRACKET) {
            n.array = new ArrayTypeNode();
            n.array.accept(this);
        }
    }

    /**
     * array -> '[' num ']' [array]
     */
    @Override
    public void visit(ArrayTypeNode n) {
        match(Tag.LBRACKET);
        if (look.tag != Tag.NUM)
            throw new SyntaxException("expected integer array size instead of '" + look + "'");
        n.size = new NumNode();
        n.size.accept(this);
        match(Tag.RBRACKET);

        if (look.tag == Tag.LBRACKET) {
            n.type = new ArrayTypeNode();
            n.type.accept(this);
        }
    }

    // Statement Nodes
    ///////////////////////////////////////////////////////////////////////////////

    public StatementNode parseStatement(){
        StatementNode n = switch (look.tag) {
            case Tag.ID -> new AssignmentNode();
            case Tag.IF -> new IfNode();
            case Tag.WHILE -> new WhileNode();
            case Tag.DO -> new DoWhileNode();
            case Tag.BREAK -> new BreakNode();
            case Tag.LBRACE -> new BlockNode(enclosingBlock );
            case Tag.EOF -> throw new SyntaxException("unexpected end of file; did you miss a closing brace?");
            default ->
                    throw new SyntaxException("invalid start of a statement '" + lexer.convertTagToString(look.tag) + "'");
        };
        n.accept(this);
        return n;
    }

    /**
     * assignment -> loc '=' expression ';'
     */
    @Override
    public void visit(AssignmentNode n) {
        n.setLine(lexer.getCurrentLine());
        n.left = new LocNode();
        n.left.accept(this);

        match(Tag.ASSIGN);
        n.expression = parseExpression();
        match(Tag.SEMICOLON);
    }

    /**
     * if -> 'if' '(' expression ')' statement ['else' statement]
     */
    @Override
    public void visit(IfNode n) {
        match(Tag.IF);
        match(Tag.LPAREN);

        n.expression = parseExpression();
        match(Tag.RPAREN);
        n.thenStatement = parseStatement();

        if (look.tag == Tag.ELSE) {
            match(Tag.ELSE);
            n.elseStatement = parseStatement();
        }
    }

    /**
     * while -> 'while' '(' expression ')' statement
     */
    @Override
    public void visit(WhileNode n) {
        match(Tag.WHILE);
        match(Tag.LPAREN);
        n.expression = parseExpression();
        match(Tag.RPAREN);

        loopCount++;
        n.body = parseStatement();
        loopCount--;
    }

    /**
     * do -> 'do' statement 'while' '(' expression ')' ';'
     */
    @Override
    public void visit(DoWhileNode n) {
        match(Tag.DO);

        loopCount++;
        n.body = parseStatement();
        loopCount--;

        match(Tag.WHILE);
        match(Tag.LPAREN);
        n.expression = parseExpression();
        match(Tag.RPAREN);
        match(Tag.SEMICOLON);
    }


    /**
     * loc -> id [array]
     */
    @Override
    public void visit(LocNode n) {
        int line = lexer.getCurrentLine();
        if (top.get((Word) look) == null) {
            throw new SyntaxException("'" + look + "' is not declared");
        }
        // Use previously created IdNode from symbol table.
        n.id = top.get((Word) look).id;
        move();

        if (look.tag == Tag.LBRACKET) {
            n.array = new ArrayLocNode();
            n.array.accept(this);
        }
        n.setLine(line);
    }

    /**
     * array -> '[' expression ']' [array]
     */
    @Override
    public void visit(ArrayLocNode n) {
        int line = lexer.getCurrentLine();
        match(Tag.LBRACKET);
        n.expression = parseExpression();
        match(Tag.RBRACKET);

        if (look.tag == Tag.LBRACKET) {
            n.array = new ArrayLocNode();
            n.array.accept(this);
        }
        n.setLine(line);
    }

    // Expression Helper Methods
    ///////////////////////////////////////////////////////////////////////////////

    boolean isBinaryOperation (int op) {
        return (op == Tag.MUL || op == Tag.DIV || op == Tag.ADD || op == Tag.SUB ||
                op == Tag.LE || op == Tag.LT || op == Tag.GE ||
                op == Tag.GT || op == Tag.EQ || op == Tag.NE || op == Tag.AND || op == Tag.OR);
    }

    int getPrecendence(int op) {
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

    // Expression Nodes
    ///////////////////////////////////////////////////////////////////////////////

   public ExpressionNode parseExpression() {
       ExpressionNode n = parseFactor();

        if (isBinaryOperation(look.tag)) {
            return parseBinaryExpression(n, 0);
        } else {
            return n;
        }
    }

    public ExpressionNode parseBinaryExpression(ExpressionNode lhs, int precedence) {
        while (isBinaryOperation(look.tag) && (getPrecendence(look.tag) >= precedence)) {
            Token op = look;
            move();
            ExpressionNode rhs = parseExpression();

            while (getPrecendence(look.tag) > getPrecendence(op.tag)) {
                rhs = parseBinaryExpression(rhs, getPrecendence(look.tag));
            }
            lhs = new BinaryExpressionNode(lhs, rhs, op.toString());
            lhs.accept(this);
        }
        return lhs;
    }

    @Override
    public void visit(BinaryExpressionNode n) {
        n.setLine(lexer.getCurrentLine());
    }

    /**
     * unary -> '-' factor | '!' factor
     */
    @Override
    public void visit(UnaryNode n) {
        int line = lexer.getCurrentLine();
        n.op = look;
        match(look.tag);
        n.right = parseFactor();
        n.setLine(line);
    }

    /**
     * factor -> parenthesis | num | real | loc | 'true' | 'false'
     */
    public ExpressionNode parseFactor() {
       ExpressionNode n;
       if (look.tag == Tag.SUB || look.tag == Tag.NOT) {
           n = new UnaryNode();
           n.accept(this);
           return n;
       }
       n = switch (look.tag) {
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
     * parenthesis -> '(' expression ')'
     */
    @Override
    public void visit(ParenthesisNode n) {
        match(Tag.LPAREN);
        n.expression = parseExpression();
        match(Tag.RPAREN);
    }

    // Terminal Nodes
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * basic -> 'int' | 'float'
     */
    @Override
    public void visit(BasicNode n) {
        n.type = (Type)look;
        match(Tag.BASIC);
    }

    /**
     * break -> 'break' ';'
     */
    @Override
    public void visit(BreakNode n) {
        if (loopCount == 0)
            throw new SyntaxException("'break' outside of loop");
        match(Tag.BREAK);
        match(Tag.SEMICOLON);
    }

    /**
     * false -> 'false'
     */
    @Override
    public void visit(FalseNode n) {
        match(Tag.FALSE);
    }

    /**
     * id -> word
     */
    @Override
    public void visit(IdNode n) {
        n.id = look.toString();
        n.w = (Word)look;
        match(Tag.ID);
    }

    /**
     * num -> integer number
     */
    @Override
    public void visit(NumNode n) {
        if (look.tag != Tag.NUM)
            throw new SyntaxException("expected int literal instead of '" + look + "'");
        n.num = ((Num)look).value;
        match(Tag.NUM);
    }

    /**
     * real -> real number
     */
    @Override
    public void visit(RealNode n) {
        n.value = ((Real)look).value;
        match(Tag.REAL);
    }

    /**
     * true -> 'true'
     */
    @Override
    public void visit(TrueNode n) {
        match(Tag.TRUE);
    }
}
