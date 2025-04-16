package noema;

import java.util.ArrayList;
import java.util.List;

/**
 * Error reporting and handling for the Noema language
 */
public class ErrorReporter {
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;
    
    private static final List<String> errors = new ArrayList<>();
    private static final List<String> warnings = new ArrayList<>();
    
    public static void reset() {
        hadError = false;
        hadRuntimeError = false;
        errors.clear();
        warnings.clear();
    }
    
    public static boolean hadError() {
        return hadError;
    }
    
    public static boolean hadRuntimeError() {
        return hadRuntimeError;
    }
    
    public static List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public static List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    public static void error(int line, String message) {
        report(line, "", message);
        hadError = true;
    }
    
    public static void error(Token token, String message) {
        if (token.getType() == Token.Type.EOF) {
            report(token.getLine(), " at end", message);
        } else {
            report(token.getLine(), " at '" + token.getLexeme() + "'", message);
        }
        hadError = true;
    }
    
    public static void warning(int line, String message) {
        String formattedMessage = String.format("[line %d] Warning: %s", line, message);
        System.err.println(formattedMessage);
        warnings.add(formattedMessage);
    }
    
    public static void runtimeError(RuntimeError error) {
        String message = String.format("[line %d] Runtime Error: %s", 
                error.getToken().getLine(), error.getMessage());
        System.err.println(message);
        errors.add(message);
        hadRuntimeError = true;
    }
    
    private static void report(int line, String where, String message) {
        String formattedMessage = String.format("[line %d] Error%s: %s", line, where, message);
        System.err.println(formattedMessage);
        errors.add(formattedMessage);
    }
    
    public static class RuntimeError extends RuntimeException {
        private final Token token;
        
        public RuntimeError(Token token, String message) {
            super(message);
            this.token = token;
        }
        
        public Token getToken() {
            return token;
        }
    }
}