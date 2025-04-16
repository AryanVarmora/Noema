package noema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Remove this import - it's causing issues
// import Noema.Token;

/**
 * Lexical analyzer for the Noema language
 */
public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    
    private int start = 0;
    private int current = 0;
    private int line = 1;
    
    // Initialize the keywords map properly
    private static final Map<String, Token.Type> keywords;
    
    static {
        keywords = new HashMap<>();
        keywords.put("fact", Token.Type.FACT);
        keywords.put("rule", Token.Type.RULE);
        keywords.put("scene", Token.Type.SCENE);
        keywords.put("npc", Token.Type.NPC);
        keywords.put("when", Token.Type.WHEN);
        keywords.put("responds", Token.Type.RESPONDS);
        keywords.put("feels", Token.Type.FEELS);
        keywords.put("if", Token.Type.IF);
        keywords.put("and", Token.Type.AND);
        keywords.put("or", Token.Type.OR);
        keywords.put("within", Token.Type.WITHIN);
        keywords.put("before", Token.Type.BEFORE);
        keywords.put("after", Token.Type.AFTER);
        keywords.put("minute", Token.Type.MINUTE);
        keywords.put("hour", Token.Type.HOUR);
        keywords.put("hours", Token.Type.HOUR);
        keywords.put("day", Token.Type.DAY);
        keywords.put("days", Token.Type.DAY);
        keywords.put("player", Token.Type.PLAYER);
        keywords.put("says", Token.Type.SAYS);
    }
    
    public Lexer(String source) {
        this.source = source;
    }
    
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        
        tokens.add(new Token(Token.Type.EOF, "", null, line));
        return tokens;
    }
    
    private void scanToken() {
        char c = advance();
        
        switch (c) {
            case '(': addToken(Token.Type.OPEN_PAREN); break;
            case ')': addToken(Token.Type.CLOSE_PAREN); break;
            case '{': addToken(Token.Type.OPEN_BRACE); break;
            case '}': addToken(Token.Type.CLOSE_BRACE); break;
            case ',': addToken(Token.Type.COMMA); break;
            case ';': addToken(Token.Type.SEMICOLON); break;
            case '=': addToken(Token.Type.EQUALS); break;
            case '"': string(); break;
            
            case '/':
                if (match('/')) {
                    // Comment goes until the end of the line
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    // Not a comment
                    // Handle division or other operators if needed
                }
                break;
                
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;
                
            case '\n':
                line++;
                break;
                
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    // Error handling
                    System.err.println("Unexpected character: " + c + " at line " + line);
                }
                break;
        }
    }
    
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        
        String text = source.substring(start, current);
        Token.Type type = keywords.get(text);
        if (type == null) type = Token.Type.IDENTIFIER;
        
        addToken(type);
    }
    
    private void number() {
        while (isDigit(peek())) advance();
        
        // Look for a decimal part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();
            
            while (isDigit(peek())) advance();
        }
        
        addToken(Token.Type.NUMBER, Double.parseDouble(source.substring(start, current)));
    }
    
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        
        if (isAtEnd()) {
            // Unterminated string
            System.err.println("Unterminated string at line " + line);
            return;
        }
        
        // The closing "
        advance();
        
        // Trim the surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(Token.Type.STRING, value);
    }
    
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        
        current++;
        return true;
    }
    
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }
    
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    private char advance() {
        return source.charAt(current++);
    }
    
    private void addToken(Token.Type type) {
        addToken(type, null);
    }
    
    private void addToken(Token.Type type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
    
    private boolean isAtEnd() {
        return current >= source.length();
    }
}