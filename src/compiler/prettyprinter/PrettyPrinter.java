/**
 * **DEPRECATED**
 * This class is no longer used in the project and was created for an earlier version
 * of the compiler where the goal was to pretty print the AST. It does not properly work
 * with the current version of the AST, but remains for reference.
 */
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

/**
 * A class that prints the AST in a pretty format where each node is indented based on its depth in the tree,
 * and braces are placed around blocks.
 *
 * When a block has only one statement within a conditional, the braces are omitted.
 */
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

    /**
     * Create a pretty printer and traverse the AST to print it to the console.
     *
     * @param parser The parser containing the AST to print.
     */
    public PrettyPrinter(Parser parser) {
        conditionalStack.add(false);
        this.parser = parser;
        writer = new PrintWriter(System.out);
        parser.program.accept(this);
        writer.close();
    }

    /**
     * Create a pretty printer and traverse the AST to print it to a file.
     *
     * @param parser The parser containing the AST to print.
     * @param filename The name of the file to write the AST to.
     */
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

    /**
     * Print a string to the output.
     *
     * @param string The string to print.
     */
    void print(String string) {
        writer.print(string);
        newLine = false;
    }

    /**
     * Print a string to the output followed by a newline.
     *
     * @param string The string to print.
     */
    void println(String string) {
        writer.println(string);
        newLine = true;
    }

    /**
     * Print the indentation if it is a new line.
     */
    void printIndent() {
        // Only print indent if it is a new line.
        if (newLine)
            for (int i = 0; i < indent * indentSpaces; i++)
                print(" ");
    }

    /**
     * Print a statement node with the appropriate indentation.
     *
     * @param node The statement node to print.
     */
    void printStatement(StatementNode node) {
        if (node instanceof BlockNode) {
            print(" ");
            node.accept(this);
        } else {
            println("");
            indent++;
            node.accept(this);
            indent--;
        }
    }

    /**
     * Visit a program node and visit the block node.
     *
     * @param node The program node to visit.
     */
    @Override
    public void visit(ProgramNode node) {
        node.block.accept(this);
    }

    /**
     * Visit a block node and visit the declarations and statements.
     *
     * Print braces around the block and indent the contents.
     *
     * @param node The block node to visit.
     */
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

    /**
     * Visit a declaration node and visit the type and id.
     *
     * @param node The declaration node to visit.
     */
    @Override
    public void visit(DeclNode node) {
        printIndent();
        node.type.accept(this);
        print(" ");
        node.id.accept(this);
        println(" ;");
    }

    /**
     * Visit a type node and print the type and visit the array if it exists.
     *
     * @param node The type node to visit.
     */
    @Override
    public void visit(TypeNode node) {
        print(node.type.toString());
        if (node.array != null)
            node.array.accept(this);
    }

    /**
     * Visit an array type node and print the size and visit any additional array dimensions after.
     *
     * @param node The array type node to visit.
     */
    @Override
    public void visit(ArrayTypeNode node) {
        print("[");
        node.size.accept(this);
        print("]");
        if (node.type != null)
            node.type.accept(this);
    }

    /**
     * Visit an assignment node and visit the left and right expressions.
     *
     * @param node The assignment node to visit.
     */
    @Override
    public void visit(AssignmentNode node) {
        printIndent();
        node.left.accept(this);
        print(" = ");
        node.expression.accept(this);
        println(" ;");
    }

    /**
     * Visit an if node and visit the expression and then and optional else statements.
     *
     * @param node The if node to visit.
     */
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

    /**
     * Visit the while node and visit the expression and the body.
     *
     * @param node The while node to visit.
     */
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

    /**
     * Visit the do while node and visit the body and the expression.
     *
     * @param node The do while node to visit.
     */
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

    /**
     * Visit a loc node and visit the id and array if it exists.
     *
     * @param node The for node to visit.
     */
    @Override
    public void visit(LocNode node) {
        node.id.accept(this);
        if (node.array != null) {
            node.array.accept(this);
        }
    }

    /**
     * Visit the array node and any additional array dimensions.
     *
     * @param node The array node to visit.
     */
    @Override
    public void visit(ArrayLocNode node) {
        print("[");
        node.expression.accept(this);
        print("]");

        if (node.array != null) {
            node.array.accept(this);
        }
    }

    /**
     * Visit the binary expression node and visit the left and right expressions.
     *
     * @param node The binary expression node to visit.
     */
    @Override
    public void visit(BinaryExpressionNode node) {
        node.left.accept(this);
        print(" ");
        print(node.operator);
        print(" ");
        node.right.accept(this);
    }

    /**
     * Visit the unary node and visit the expression.
     *
     * @param node The unary node to visit.
     */
    @Override
    public void visit(UnaryNode node) {
        print(node.operator.toString());
        node.expression.accept(this);
    }

    /**
     * Visit the parenthesis node and visit the expression.
     *
     * @param node The parenthesis node to visit.
     */
    @Override
    public void visit(ParenthesisNode node) {
        print("(");
        node.expression.accept(this);
        print(")");
    }

    /**
     * Visit the break node and print 'break ;'.
     *
     * @param node The break node to visit.
     */
    @Override
    public void visit(BreakNode node) {
        printIndent();
        println("break ;");
    }

    /**
     * Visit the false node and print 'false'.
     *
     * @param node The false node to visit.
     */
    @Override
    public void visit(FalseNode node) {
        print("false");
    }

    /**
     * Visit the true node and print 'true'.
     *
     * @param node The true node to visit.
     */
    @Override
    public void visit(TrueNode node) {
        print("true");
    }

    /**
     * Visit the IdNode and print the identifier string.
     *
     * @param node The IdNode to visit.
     */
    @Override
    public void visit(IdNode node) {
        print(node.id);
    }

    /**
     * Visit the num node and print the number.
     *
     * @param node The num node to visit.
     */
    @Override
    public void visit(NumNode node) {
        print("" + node.num);
    }

    /**
     * Visit the real node and print the number.
     *
     * @param node The real node to visit.
     */
    @Override
    public void visit(RealNode node) {
        print(node.toString());
    }
}
