package noema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filename = args.length > 0 ? args[0] : "examples/Example.noema";
        
        try {
            System.out.println("Reading file: " + filename);
            String source = new String(Files.readAllBytes(Paths.get(filename)));
            
            // Lexical analysis only
            System.out.println("\nStarting lexical analysis...");
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();
            
            // Print tokens
            System.out.println("\nTokens:");
            for (Token token : tokens) {
                System.out.println(token);
            }
            
            System.out.println("\nLexical analysis completed successfully.");
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}