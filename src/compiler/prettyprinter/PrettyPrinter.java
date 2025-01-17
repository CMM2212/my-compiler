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
    public void visit(ProgramNode node) {
        node.block.accept(this);
    }

    @Override
    public void visit(BlockNode node) {
        controlStack.add(false);
        printIndent();
        println("{");

        for (DeclNode decl : node.decls) {
            indent++;
            decl.accept(this);
            indent--;
        }

        if (!node.decls.isEmpty())
            println("");

        conditionalStack.add(false);


        for (StatementNode statement : node.statements) {
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
    public void visit(DeclNode node) {
        printIndent();
        node.type.accept(this);
        print(" ");
        node.id.accept(this);
        println(" ;");
    }

    @Override
    public void visit(TypeNode node) {
        print(node.type.toString());
        if (node.array != null)
            node.array.accept(this);
    }

    @Override
    public void visit(ArrayTypeNode node) {
        print("[");
        node.size.accept(this);
        print("]");
        if (node.type != null)
            node.type.accept(this);
    }

    // Statement Nodes
    ///////////////////////////////////////////////////////////////////////////////
    @Override
    public void visit(AssignmentNode node) {
        printIndent();
        node.left.accept(this);
        print(" = ");
        node.expression.accept(this);
        println(" ;");
    }

    @Override
    public void visit(IfNode node) {
        printIndent();
        print("if (");
        node.expression.accept(this);
        print(")");

        conditionalStack.add(true);
        controlStack.add(false);
        printStatement(node.thenStatement);
        conditionalStack.pop();
        controlStack.pop();


        if (node.elseStatement != null) {
            if (node.elseStatement instanceof IfNode) {
                printIndent();
                print("else ");
                node.elseStatement.accept(this);
            } else {
                printIndent();
                print("else");

                conditionalStack.add(false);
                controlStack.add(false);
                printStatement(node.elseStatement);
                conditionalStack.pop();
                controlStack.pop();
            }
        } else if (node.thenStatement instanceof BlockNode) {
            println("");
        }
    }

    @Override
    public void visit(WhileNode node) {
        printIndent();
        print("while (");
        node.expression.accept(this);
        print(")");

        conditionalStack.add(false);
        controlStack.add(false);
        printStatement(node.body);
        controlStack.pop();
        conditionalStack.pop();
    }

    @Override
    public void visit(DoWhileNode node) {
        printIndent();
        print("do");

        conditionalStack.add(true);
        controlStack.add(false);
        printStatement(node.body);
        conditionalStack.pop();
        controlStack.pop();

        printIndent();
        print("while (");
        node.expression.accept(this);
        println(") ;");
    }

    @Override
    public void visit(LocNode node) {
        node.id.accept(this);
        if (node.array != null) {
            node.array.accept(this);
        }
    }

    @Override
    public void visit(ArrayLocNode node) {
        print("[");
        node.expression.accept(this);
        print("]");

        if (node.array != null) {
            node.array.accept(this);
        }
    }

    // Expression Nodes
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(BinaryExpressionNode node) {
        node.left.accept(this);
        print(" ");
        print(node.operator);
        print(" ");
        node.right.accept(this);
    }

    @Override
    public void visit(UnaryNode node) {
        if (node.operator != null ) {
            print(node.operator.toString());
        }
        node.expression.accept(this);
    }

    // Terminal Nodes
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ParenthesisNode node) {
        print("(");
        node.expression.accept(this);
        print(")");
    }

    @Override
    public void visit(BreakNode node) {
        printIndent();
        println("break ;");
    }

    @Override
    public void visit(FalseNode node) {
        print("false");
    }

    @Override
    public void visit(IdNode node) {
        print(node.id);
    }

    @Override
    public void visit(NumNode node) {
        print("" + node.num);
    }

    @Override
    public void visit(RealNode node) {
        print(node.toString());
    }

    @Override
    public void visit(TrueNode node) {
        print("true");
    }
}
