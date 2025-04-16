package noema;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced condition parser for Noema
 * Handles complex conditions with multiple clauses and operators
 */
public class ConditionParser {
    private final List<Token> tokens;
    private int current = 0;
    
    public ConditionParser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public AST.Condition parse() {
        AST.Condition condition = new AST.Condition(new ArrayList<>(), new ArrayList<>());
        
        // Parse first expression
        condition.conditions.add(expression());
        
        // Parse additional expressions with operators
        while (match(Token.Type.AND, Token.Type.OR, Token.Type.EQUALS, 
                     Token.Type.NOT_EQUALS, Token.Type.GREATER_THAN, Token.Type.LESS_THAN)) {
            String operator = previous().getLexeme();
            condition.operators.add(operator);
            condition.conditions.add(expression());
        }
        
        return condition;
    }
    
    private AST.Expression expression() {
        // Handle literals
        if (match(Token.Type.STRING, Token.Type.NUMBER)) {
            return new AST.Expression(previous().getLiteral());
        }
        
        // Handle identifiers and function calls
        if (match(Token.Type.IDENTIFIER)) {
            String name = previous().getLexeme();
            
            // Check if this is a function call
            if (check(Token.Type.OPEN_PAREN)) {
                return functionCall(name);
            }
            
            return new AST.Expression(name);
        }
        
        // Handle parenthesized expressions
        if (match(Token.Type.OPEN_PAREN)) {
            AST.Expression expr = new AST.Expression(parse());
            consume(Token.Type.CLOSE_PAREN, "Expected ')' after expression.");
            return expr;
        }
        
        // Error handling
        throw new ErrorReporter.RuntimeError(peek(), "Expected expression.");
    }
    
    private AST.Expression functionCall(String name) {
        consume(Token.Type.OPEN_PAREN, "Expected '(' after function name.");
        
        List<AST.Expression> arguments = new ArrayList<>();
        
        if (!check(Token.Type.CLOSE_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(Token.Type.COMMA));
        }
        
        Token closeParen = consume(Token.Type.CLOSE_PAREN, "Expected ')' after arguments.");
        
        return new AST.Expression(new AST.FunctionCall(name, arguments));
    }
    
    // Helper methods similar to Parser.java
    
    private boolean match(Token.Type... types) {
        for (Token.Type type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    
    private Token consume(Token.Type type, String message) {
        if (check(type)) return advance();
        
        throw new ErrorReporter.RuntimeError(peek(), message);
    }
    
    private boolean check(Token.Type type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }
    
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private boolean isAtEnd() {
        return peek().getType() == Token.Type.EOF;
    }
    
    private Token peek() {
        return tokens.get(current);
    }
    
    private Token previous() {
        return tokens.get(current - 1);
    }
}