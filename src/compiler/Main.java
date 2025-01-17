package compiler;

import compiler.errors.*;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
import compiler.prettyprinter.PrettyPrinter;
import compiler.tac.IntermediateCodeGenerator;
import compiler.tac.IntermediateCodePrinter;
import compiler.typechecker.TypeChecker;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        String filename = "input.txt";
        Lexer lexer = new Lexer(filename);
        ErrorPrinter errorPrinter = new ErrorPrinter();
        try {
            Parser parser = new Parser(lexer);
            TypeChecker typeChecker = new TypeChecker(parser);
            //PrettyPrinter printer = new PrettyPrinter(parser);
            IntermediateCodeGenerator interCode = new IntermediateCodeGenerator(typeChecker);
            IntermediateCodePrinter interCodePrinter = new IntermediateCodePrinter(interCode);
            System.out.println("Starting lexical analysis...\n");
        }
        catch (CompilerException e) {
                if (e instanceof LexicalException || e instanceof SyntaxException) {
                    errorPrinter.printError(lexer.getErrorContext(), e);
                } else if (e instanceof TypeException) {
                    errorPrinter.printError(lexer.getErrorContext().lines, lexer.getErrorContext(), (TypeException) e);
                }
        }
    }
}
