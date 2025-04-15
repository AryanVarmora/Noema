package NOEMA;

/**
 * Represents a token in the Noema language
 */
public class Token {
    public enum Type {
        // Keywords
        FACT, RULE, SCENE, NPC, WHEN, RESPONDS, FEELS, IF, AND, WITHIN, MINUTE,
        
        // Identifiers and literals
        IDENTIFIER, STRING, NUMBER,
        
        // Operators
        EQUALS, GREATER_THAN, LESS_THAN, NOT_EQUALS,
        
        // Punctuation
        OPEN_BRACE, CLOSE_BRACE, OPEN_PAREN, CLOSE_PAREN, 
        COMMA, SEMICOLON, QUOTE,
        
        // End of file
        EOF
    }
    
    private final Type type;
    private final String lexeme;
    private final Object literal;
    private final int line;
    
    public Token(Type type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }
    
    public Type getType() {
        return type;
    }
    
    public String getLexeme() {
        return lexeme;
    }
    
    public Object getLiteral() {
        return literal;
    }
    
    public int getLine() {
        return line;
    }
    
    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}