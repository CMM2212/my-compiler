package compiler.argparse;

public record ArgumentParser(String inputFilename, String outputFilename) {

    public static ArgumentParser parseArguments(String[] args) {
        // Default values
        String inputFile = "input.txt";
        String outputFile = null;

        for (int i = 0; i < args.length; i++)
            switch (args[i]) {
                case "-i":
                case "--input":
                    inputFile = args[++i];
                case "-o":
                case "--output":
                    outputFile = args[++i];
                case "-h":
                case "--help":
                    printHelp();
                    System.exit(0);
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    printHelp();
                    System.exit(1);
            }
        return new ArgumentParser(inputFile, outputFile);
    }

    private static void printHelp() {
        System.out.println("Usage: my-compiler [options]");
        System.out.println("Options:");
        System.out.println("  -i, --input <file>    Specify the input file to compile.");
        System.out.println("  -o, --output <file>   Specify the output file to write the compiled code to.");
        System.out.println("  -h, --help            Print this help message.");
    }
}
