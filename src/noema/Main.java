package noema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filename = args.length > 0 ? args[0] : "examples/Example.noema";

        try {
            System.out.println("📄 Reading file: " + filename);
            String source = Files.readString(Paths.get(filename));

            // Lexical analysis
            System.out.println("\n🔍 Starting lexical analysis...");
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();
            for (Token token : tokens) {
                System.out.println(token);
            }

            // Parsing
            System.out.println("\n🧠 Parsing...");
            Parser parser = new Parser(tokens);
            AST.Program program = parser.parse();

            // Interpretation
            System.out.println("\n🚀 Interpreting...");
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(program);

            // Rule evaluation
            interpreter.evaluateRules();

            // Output result
            System.out.println("\n✅ Execution complete.");
            System.out.println("📦 Global Variables:");
            interpreter.getVariableNames().forEach(var ->
                System.out.println("  " + var + " = " + interpreter.getVariable(var))
            );

        } catch (IOException e) {
            System.err.println("❌ Error reading file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Runtime error: " + e.getMessage());
        }
    }
}
