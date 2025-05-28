package compiler;

import compiler.argparse.ArgumentParser;
import compiler.errors.*;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
import compiler.tac.IntermediateCodeGenerator;
import compiler.tac.IntermediateCodePrinter;
import compiler.typechecker.TypeChecker;

/**
 * Entry point for the compiler.
 *
 *
 */
public class Main {
    public static void main(String[] args) {
        ArgumentParser argumentParser = ArgumentParser.parseArguments(args);
        Lexer lexer = new Lexer(argumentParser.inputFilename());

        try {
            compile(lexer, argumentParser.outputFilename());
        } catch (CompilerException e) {
            printError(e, lexer);
            System.exit(1);
        }
    }

    private static void compile(Lexer lexer, String outputFilename) {
        Parser parser = new Parser(lexer);
        TypeChecker typeChecker = new TypeChecker(parser);
        IntermediateCodeGenerator interCode = new IntermediateCodeGenerator(typeChecker);
        IntermediateCodePrinter interCodePrinter = new IntermediateCodePrinter(interCode, outputFilename);
    }

    private static void printError(CompilerException e, Lexer lexer){
        ErrorPrinter errorPrinter = new ErrorPrinter();
            if (e instanceof LexicalException || e instanceof SyntaxException) {
                errorPrinter.printError(lexer.getErrorContext(), e);
            } else if (e instanceof TypeException) {
                errorPrinter.printError(lexer.getErrorContext().lines, lexer.getErrorContext(), (TypeException) e);
            }
    }
}
