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
    public void visit(ProgramNode n) {
        n.block.accept(this);
    }

    @Override
    public void visit(BlockNode n) {
        for (StatementNode statement : n.statements) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(AssignmentNode n) {
        n.left.accept(this);
        print("=");
        n.expression.accept(this);
        println("");
    }

    @Override
    public void visit(WhileNode n) {
        print("while (");
        n.expression.accept(this);
        print(")");

        printStatement(n.body);
    }

    @Override
    public void visit(DoWhileNode n) {
        print("do");

        printStatement(n.body);

        print("while (");
        n.expression.accept(this);
        println(") ;");
    }

    @Override
    public void visit(IfFalseNode n) {
        print(" iffalse ");
        n.expression.accept(this);
    }

    @Override
    public void visit(IfTrueNode n) {
        print( " if ");
        n.expression.accept(this);
        print(" ");
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
        print("] ");

        if (n.array != null) {
            n.array.accept(this);
        }
    }

    @Override
    public void visit(BinaryExpressionNode n) {
        n.left.accept(this);
        print(n.operator);
        n.right.accept(this);
    }

    @Override
    public void visit(UnaryNode n) {
        if (n.op != null ) {
            print(n.op.toString());
        }
        n.right.accept(this);
    }

    @Override
    public void visit(ParenthesisNode n) {
        print("(");
        n.expression.accept(this);
        print(")");
    }

    @Override
    public void visit(BreakNode n) {
        println("break ;");
    }

    @Override
    public void visit(FalseNode n) {
        print(" false ");
    }

    @Override
    public void visit(IdNode n) {
        print(" " + n.id + " ");
    }

    @Override
    public void visit(NumNode n) {
        print(" " + n.num + " ");
    }

    @Override
    public void visit(RealNode n) {
        print(" " + n.toString() + " ");
    }

    @Override
    public void visit(TrueNode n) {
        print(" true ");
    }

    @Override
    public void visit(GotoNode n) {
        print(" goto ");
        print (n.label.id);
        println("");
    }

    @Override
    public void visit(LabelNode n) {
        printLabel(n.id + ":");
    }

    @Override
    public void visit(TempNode n) {
        print(" " + n.id + " ");
    }
}
