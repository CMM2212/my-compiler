package compiler.tac;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.*;
import compiler.parser.ast.nodes.expressions.*;
import compiler.parser.ast.nodes.expressions.operations.*;
import compiler.parser.ast.nodes.statements.*;
import compiler.parser.ast.nodes.terminals.*;

import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * A class that prints the intermediate three-address code (TAC) to a file.
 *
 * This class is responsible for printing the three address code to a file. It will
 * format the code in a way where labels are on the left, and the statements are tabbed
 * to the right.
 *
 * Example:
 * L2:     iffalse  x > 5  goto L1
 *         x = x + 10
 * L1:     y = x - 5
 */
public class IntermediateCodePrinter implements ASTVisitor {
    // Amount of spaces before statements begin on each line.
    int indentSpaces = 7;
    // This is important for taking into account how long the current line already is
    // for when providing the statement indent. For example if the current line is 6
    // characters, and indentSpaces is 7, then only 1 additional space is needed to indent.
    int currentLineLength = 0;
    // Keeps track of whether the current print command is for a label.
    // This signals that it should not indent the label when print is called.
    boolean printingLabel = false;

    // Keeps track of whether it is currently a new empty line.
    // (Makes sure indents are only printed on new lines)
    boolean newLine = true;

    // The current indent amount.
    PrintWriter writer;

    public IntermediateCodePrinter(IntermediateCodeGenerator interCode) {
        writer = new PrintWriter(System.out);
        for (StatementNode statement : interCode.currentStatements)
            statement.accept(this);
        writer.close();
    }

    /**
     * Create a new intermediate code printer with the given intermediate code generator and
     * write the output to the given filename.
     *
     * This will attempt to immediately parse the intermediate code and write it to the
     * given filename.
     *
     * @param interCode The intermediate code generator that has a list of statements generated.
     * @param filename The filename to write the intermediate code to.
     */
    public IntermediateCodePrinter(IntermediateCodeGenerator interCode, String filename) {
        try {
            writer = new PrintWriter(new FileWriter(filename));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }

        for (StatementNode statement : interCode.currentStatements)
            statement.accept(this);

        writer.close();
    }

    /**
     * Print a string to the output file.
     *
     * This will add an indent if it is a new line, and format the label if it is
     * printing a label.
     *
     * @param string The string to print.
     */
    void print(String string) {
        if (newLine) {
            printIndents();
        } else if (printingLabel) {
            for (int i = 0; i < indentSpaces - currentLineLength; i++) {
                writer.print(" ");
            }
        }
        writer.print(string);
        printingLabel = false;
        newLine = false;
    }

    /**
     * Print a new line character to the output file.
     */
    void printNewLine() {
        writer.println();
        currentLineLength = 0;
        newLine = true;
    }

    /**
     * Print a label to the output file.
     *
     * This will add an indent if it is a new line.
     *
     * @param string The label to print.
     */
    void printLabel(String string) {
        printingLabel = true;
        writer.print(string);
        currentLineLength += string.length();
        newLine = false;
    }

    /**
     * Print the current indent amount to the output file.
     */
    void printIndents() {
        for (int i = 0; i < indentSpaces; i++) {
            writer.print(" ");
        }
    }

    /**
     * Print an assignment node by printing the left identifier/array, an equals sign,
     * and then the right expression to the output file.
     * @param node The assignment node to print.
     */
    @Override
    public void visit(AssignmentNode node) {
        node.left.accept(this);
        print("=");
        node.expression.accept(this);
        // Assignment node will always end a line.
        printNewLine();
    }

    /**
     * Print iffalse node and the expression to the output file.
     *
     * @param node The if false node to print.
     */
    @Override
    public void visit(IfFalseNode node) {
        print(" iffalse ");
        node.expression.accept(this);
    }

    /**
     * Print if true node and the expression to the output file.
     *
     * @param node The if true node to print.
     */
    @Override
    public void visit(IfTrueNode node) {
        print( " if ");
        node.expression.accept(this);
        print(" ");
    }

    /**
     * Print the identifier name and array offset if it exists to the output file.
     *
     * @param node The loc node to print.
     */
    @Override
    public void visit(LocNode node) {
        node.id.accept(this);
        if (node.array != null)
            node.array.accept(this);
    }

    /**
     * Print the array access component of the loc node by printing the expression in brackets
     * to the output file.
     *
     * @param node The array loc node to print.
     */
    @Override
    public void visit(ArrayLocNode node) {
        print("[");
        node.expression.accept(this);
        print("] ");
    }

    /**
     * Print the left expression, the operator, and the right expression to the output file.
     *
     * @param node The binary node to print.
     */
    @Override
    public void visit(BinaryExpressionNode node) {
        node.left.accept(this);
        print(node.operator);
        node.right.accept(this);
    }

    /**
     * Print the unary operator and expression to the output file.
     *
     * @param node The unary node to print.
     */
    @Override
    public void visit(UnaryNode node) {
        print(node.operator.toString());
        node.expression.accept(this);
    }

    /**
     * Print false literal to the output file.
     *
     * @param node The false node to print.
     */
    @Override
    public void visit(FalseNode node) {
        print(" false ");
    }

    /**
     * Print true literal to the output file.
     *
     * @param node The true node to print.
     */
    @Override
    public void visit(TrueNode node) {
        print(" true ");
    }


    /**
     * Print the identifier name to the output file.
     *
     * @param node The identifier node to print.
     */
    @Override
    public void visit(IdNode node) {
        print(" " + node.id + " ");
    }

    /**
     * Print the integer literal to the output file.
     *
     * @param node The num node to print.
     */
    @Override
    public void visit(NumNode node) {
        print(" " + node.num + " ");
    }

    /**
     * Print the floating point literal to the output file.
     *
     * @param node The real node to print.
     */
    @Override
    public void visit(RealNode node) {
        print(" " + node.toString() + " ");
    }

    /**
     * Prints a goto node with a label to the output file.
     *
     * @param node The goto node to print.
     */
    @Override
    public void visit(GotoNode node) {
        print(" goto ");
        print (node.label.id);
        // goto node will always end a line.
        printNewLine();
    }

    /**
     * Prints a label node to the output file.
     *
     * @param node The label node to print.
     */
    @Override
    public void visit(LabelNode node) {
        printLabel(node.id + ":");
    }

    /**
     * Prints a temp node to the output file.
     *
     * @param node The temp node to print.
     */
    @Override
    public void visit(TempNode node) {
        print(" " + node.id + " ");
    }
}
