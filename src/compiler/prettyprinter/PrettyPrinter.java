package compiler.prettyprinter;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.*;
import compiler.parser.ast.nodes.declarations.*;
import compiler.parser.ast.nodes.expressions.*;
import compiler.parser.ast.nodes.expressions.operations.BinaryExpressionNode;
import compiler.parser.ast.nodes.expressions.operations.UnaryNode;
import compiler.parser.ast.nodes.statements.*;
import compiler.parser.ast.nodes.structures.ProgramNode;
import compiler.parser.ast.nodes.terminals.*;
import compiler.parser.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Stack;

public class PrettyPrinter implements ASTVisitor {
    public Parser parser;

    int indent = 0;
    int indentSpaces = 4;

    // Keeps track of whether it is currently a new empty line.
    // (Makes sure indents are only printed on new lines)
    boolean newLine = true;

    // Keeps track of whether block is within a conditional statement
    // (Makes sure 'else' and 'while' appear on same line as closing brace)
    Stack<Boolean> conditionalStack = new Stack<>();

    // Keeps track of whether previous statement within a block was a control statement
    // (Makes sure there is a blank line between control statements within a block)
    Stack<Boolean> controlStack = new Stack<>();

    PrintWriter writer;

    public PrettyPrinter(Parser parser) {
        conditionalStack.add(false);
        this.parser = parser;
        writer = new PrintWriter(System.out);
        parser.program.accept(this);
        writer.close();
    }

    public PrettyPrinter(Parser parser, String filename) {
        conditionalStack.add(false);
        this.parser = parser;
        try {
            writer = new PrintWriter(new FileWriter(filename));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
        parser.program.accept(this);
        writer.close();
    }

    // Helper Methods
    ///////////////////////////////////////////////////////////////////////////////

    void print(String s) {
        writer.print(s);
        newLine = false;
    }

    void println(String s) {
        writer.println(s);
        newLine = true;
    }

    void printIndent() {
        // Only print indent if it is a new line.
        if (newLine) {
            for (int i = 0; i < indent * indentSpaces; i++) {
                print(" ");
            }
        }
    }

    void printStatement(StatementNode n) {
        if (n instanceof BlockNode) {
            print(" ");
            n.accept(this);
        } else {
            println("");
            indent++;
            n.accept(this);
            indent--;
        }
    }

    void insertSpaceBetweenControlStatements(StatementNode n) {
        boolean isControlStatement = n instanceof IfNode || n instanceof WhileNode ||
                n instanceof DoWhileNode;
        // If previous statement was a control statement, print a blank line.
        if (controlStack.peek()) {
            println("");
        }

        controlStack.pop();
        controlStack.add(isControlStatement);
    }

    // Main Nodes
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ProgramNode n) {
        n.block.accept(this);
    }

    @Override
    public void visit(BlockNode n) {
        controlStack.add(false);
        printIndent();
        println("{");

        for (DeclNode decl : n.decls) {
            indent++;
            decl.accept(this);
            indent--;
        }

        if (!n.decls.isEmpty())
            println("");

        conditionalStack.add(false);


        for (StatementNode statement : n.statements) {
            indent++;
            printStatement(statement);
            indent--;
        }
        conditionalStack.pop();

        printIndent();

        if (!conditionalStack.peek()) {
            println("}");
        } else {
            print("} ");
        }
        controlStack.pop();
    }

//    @Override
//    public void visit(DeclsNode n) {
//        if (n.decl == null) {
//            return;
//        }
//
//        n.decl.accept(this);
//
//        if (n.decls != null) {
//            n.decls.accept(this);
//        }
//    }

    @Override
    public void visit(DeclNode n) {
        printIndent();
        n.type.accept(this);
        print(" ");
        n.id.accept(this);
        println(" ;");
    }

    @Override
    public void visit(TypeNode n) {
        n.type.accept(this);
        if (n.array != null)
            n.array.accept(this);
    }

    @Override
    public void visit(ArrayTypeNode n) {
        print("[");
        n.size.accept(this);
        print("]");
        if (n.type != null)
            n.type.accept(this);
    }

    // Statement Nodes
    ///////////////////////////////////////////////////////////////////////////////
    @Override
    public void visit(AssignmentNode n) {
        printIndent();
        n.left.accept(this);
        print(" = ");
        n.expression.accept(this);
        println(" ;");
    }

    @Override
    public void visit(IfNode n) {
        printIndent();
        print("if (");
        n.expression.accept(this);
        print(")");

        conditionalStack.add(true);
        controlStack.add(false);
        printStatement(n.thenStatement);
        conditionalStack.pop();
        controlStack.pop();


        if (n.elseStatement != null) {
            if (n.elseStatement instanceof IfNode) {
                printIndent();
                print("else ");
                n.elseStatement.accept(this);
            } else {
                printIndent();
                print("else");

                conditionalStack.add(false);
                controlStack.add(false);
                printStatement(n.elseStatement);
                conditionalStack.pop();
                controlStack.pop();
            }
        } else if (n.thenStatement instanceof BlockNode) {
            println("");
        }
    }

    @Override
    public void visit(WhileNode n) {
        printIndent();
        print("while (");
        n.expression.accept(this);
        print(")");

        conditionalStack.add(false);
        controlStack.add(false);
        printStatement(n.body);
        controlStack.pop();
        conditionalStack.pop();
    }

    @Override
    public void visit(DoWhileNode n) {
        printIndent();
        print("do");

        conditionalStack.add(true);
        controlStack.add(false);
        printStatement(n.body);
        conditionalStack.pop();
        controlStack.pop();

        printIndent();
        print("while (");
        n.expression.accept(this);
        println(") ;");
    }

    @Override
    public void visit(LocNode n) {
        n.id.accept(this);
        if (n.array != null) {
            n.array.accept(this);
        }
    }

    @Override
    public void visit(ArrayLocNode n) {
        print("[");
        n.expression.accept(this);
        print("]");

        if (n.array != null) {
            n.array.accept(this);
        }
    }

    // Expression Nodes
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(BinaryExpressionNode n) {
        n.left.accept(this);
        print(" ");
        print(n.operator);
        print(" ");
        n.right.accept(this);
    }

    @Override
    public void visit(UnaryNode n) {
        if (n.op != null ) {
            print(n.op.toString());
        }
        n.right.accept(this);
    }

    // Terminal Nodes
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ParenthesisNode n) {
        print("(");
        n.expression.accept(this);
        print(")");
    }

    @Override
    public void visit(BasicNode n) {
        print(n.type.toString());
    }

    @Override
    public void visit(BreakNode n) {
        printIndent();
        println("break ;");
    }

    @Override
    public void visit(FalseNode n) {
        print("false");
    }

    @Override
    public void visit(IdNode n) {
        print(n.id);
    }

    @Override
    public void visit(NumNode n) {
        print("" + n.num);
    }

    @Override
    public void visit(RealNode n) {
        print(n.toString());
    }

    @Override
    public void visit(TrueNode n) {
        print("true");
    }
}
