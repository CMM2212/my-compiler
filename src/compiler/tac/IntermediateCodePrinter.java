package compiler.tac;

import compiler.parser.Parser;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.*;
import compiler.parser.ast.nodes.expressions.*;
import compiler.parser.ast.nodes.expressions.operations.*;
import compiler.parser.ast.nodes.statements.*;
import compiler.parser.ast.nodes.structures.ProgramNode;
import compiler.parser.ast.nodes.terminals.*;

import java.io.FileWriter;
import java.io.PrintWriter;

public class IntermediateCodePrinter implements ASTVisitor {
    public Parser parser;

    int indentSpaces = 7;
    int currentLineLength = 0;
    boolean printingLabel = false;

    // Keeps track of whether it is currently a new empty line.
    // (Makes sure indents are only printed on new lines)
    boolean newLine = true;

    PrintWriter writer;

    public IntermediateCodePrinter(IntermediateCodeGenerator interCode) {
        writer = new PrintWriter(System.out);
        for (StatementNode statement : interCode.currentStatements)
            statement.accept(this);
        writer.close();
    }

    public IntermediateCodePrinter(IntermediateCodeGenerator interCode, String filename) {
        try {
            writer = new PrintWriter(new FileWriter(filename));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
        //interCode.program.accept(this);
        for (StatementNode statement : interCode.currentStatements) {
            statement.accept(this);
        }
        writer.close();
    }

    // Helper Methods
    ///////////////////////////////////////////////////////////////////////////////

    void print(String s) {
        if (newLine) {
            printIndents();
        } else if (printingLabel) {
            for (int i = 0; i < indentSpaces - currentLineLength; i++) {
                writer.print(" ");
            }
        }
        printingLabel = false;
        writer.print(s);
        newLine = false;
    }

    void println(String s) {
        writer.println(s);
        currentLineLength = 0;
        newLine = true;
    }

    void printLabel(String s) {
        printingLabel = true;
        writer.print(s);
        currentLineLength += s.length();
        newLine = false;
    }

    void printIndents() {
        for (int i = 0; i < indentSpaces; i++) {
            writer.print(" ");
        }
    }

    void printStatement(StatementNode n) {
        if (n instanceof BlockNode) {
            print(" ");
            n.accept(this);
        } else {
            println("");
            n.accept(this);
        }
    }

    @Override
    public void visit(ProgramNode node) {
        node.block.accept(this);
    }

    @Override
    public void visit(BlockNode node) {
        for (StatementNode statement : node.statements) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(AssignmentNode node) {
        node.left.accept(this);
        print("=");
        node.expression.accept(this);
        println("");
    }

    @Override
    public void visit(WhileNode node) {
        print("while (");
        node.expression.accept(this);
        print(")");

        printStatement(node.body);
    }

    @Override
    public void visit(DoWhileNode node) {
        print("do");

        printStatement(node.body);

        print("while (");
        node.expression.accept(this);
        println(") ;");
    }

    @Override
    public void visit(IfFalseNode node) {
        print(" iffalse ");
        node.expression.accept(this);
    }

    @Override
    public void visit(IfTrueNode node) {
        print( " if ");
        node.expression.accept(this);
        print(" ");
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
        print("] ");

        if (node.array != null) {
            node.array.accept(this);
        }
    }

    @Override
    public void visit(BinaryExpressionNode node) {
        node.left.accept(this);
        print(node.operator);
        node.right.accept(this);
    }

    @Override
    public void visit(UnaryNode node) {
        print(node.operator.toString());
        node.expression.accept(this);
    }

    @Override
    public void visit(ParenthesisNode node) {
        print("(");
        node.expression.accept(this);
        print(")");
    }

    @Override
    public void visit(BreakNode node) {
        println("break ;");
    }

    @Override
    public void visit(FalseNode node) {
        print(" false ");
    }

    @Override
    public void visit(IdNode node) {
        print(" " + node.id + " ");
    }

    @Override
    public void visit(NumNode node) {
        print(" " + node.num + " ");
    }

    @Override
    public void visit(RealNode node) {
        print(" " + node.toString() + " ");
    }

    @Override
    public void visit(TrueNode node) {
        print(" true ");
    }

    @Override
    public void visit(GotoNode node) {
        print(" goto ");
        print (node.label.id);
        println("");
    }

    @Override
    public void visit(LabelNode node) {
        printLabel(node.id + ":");
    }

    @Override
    public void visit(TempNode node) {
        print(" " + node.id + " ");
    }
}
