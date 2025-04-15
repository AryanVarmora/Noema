package NOEMA;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the Noema language
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public AST.Program parse() {
        AST.Program program = new AST.Program();
        
        while (!isAtEnd()) {
            program.addStatement(declaration());
        }
        
        return program;
    }
    
    private AST.Node declaration() {
        if (match(Token.Type.FACT)) {
            return factDeclaration();
        } else if (match(Token.Type.RULE)) {
            return ruleDeclaration();
        } else if (match(Token.Type.SCENE)) {
            return sceneDeclaration();
        } else {
            // Error handling
            error(peek(), "Expected declaration.");
            synchronize();
            return null;
        }
    }
    
    private AST.Fact factDeclaration() {
        String predicate = consume(Token.Type.IDENTIFIER, "Expected fact predicate.").getLexeme();
        
        consume(Token.Type.OPEN_PAREN, "Expected '(' after fact predicate.");
        List<AST.Expression> arguments = new ArrayList<>();
        
        if (!check(Token.Type.CLOSE_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(Token.Type.COMMA));
        }
        
        consume(Token.Type.CLOSE_PAREN, "Expected ')' after arguments.");
        
        return new AST.Fact(predicate, arguments);
    }
    
    private AST.Rule ruleDeclaration() {
        String name = consume(Token.Type.IDENTIFIER, "Expected rule name.").getLexeme();
        
        consume(Token.Type.IF, "Expected 'if' after rule name.");
        AST.Condition condition = parseCondition();
        
        consume(Token.Type.OPEN_BRACE, "Expected '{' before rule body.");
        List<AST.Action> actions = new ArrayList<>();
        
        while (!check(Token.Type.CLOSE_BRACE) && !isAtEnd()) {
            actions.add(parseAction());
        }
        
        consume(Token.Type.CLOSE_BRACE, "Expected '}' after rule body.");
        
        return new AST.Rule(name, condition, actions);
    }
    
    private AST.Scene sceneDeclaration() {
        String name = consume(Token.Type.STRING, "Expected scene name.").getLexeme();
        
        consume(Token.Type.OPEN_BRACE, "Expected '{' before scene body.");
        
        List<AST.NPC> npcs = new ArrayList<>();
        List<AST.When> triggers = new ArrayList<>();
        
        while (!check(Token.Type.CLOSE_BRACE) && !isAtEnd()) {
            if (match(Token.Type.NPC)) {
                npcs.add(parseNPC());
            } else if (match(Token.Type.WHEN)) {
                triggers.add(parseTrigger());
            } else {
                // Error handling
                error(peek(), "Expected NPC or trigger declaration.");
                synchronize();
            }
        }
        
        consume(Token.Type.CLOSE_BRACE, "Expected '}' after scene body.");
        
        return new AST.Scene(name, npcs, triggers);
    }
    
    private AST.NPC parseNPC() {
        String name = consume(Token.Type.STRING, "Expected NPC name.").getLexeme();
        
        consume(Token.Type.FEELS, "Expected 'feels' after NPC name.");
        AST.Expression mood = expression();
        
        return new AST.NPC(name, mood);
    }
    
    private AST.When parseTrigger() {
        consume(Token.Type.PLAYER, "Expected 'player' after 'when'.");
        consume(Token.Type.SAYS, "Expected 'says' after 'player'.");
        
        AST.Condition condition = parseCondition();
        
        consume(Token.Type.OPEN_BRACE, "Expected '{' before trigger body.");
        List<AST.Action> actions = new ArrayList<>();
        
        while (!check(Token.Type.CLOSE_BRACE) && !isAtEnd()) {
            actions.add(parseAction());
        }
        
        consume(Token.Type.CLOSE_BRACE, "Expected '}' after trigger body.");
        
        return new AST.When(condition, actions);
    }
    
    private AST.Condition parseCondition() {
        List<AST.Expression> conditions = new ArrayList<>();
        List<String> operators = new ArrayList<>();
        
        conditions.add(expression());
        
        while (match(Token.Type.AND)) {
            operators.add("and");
            conditions.add(expression());
        }
        
        return new AST.Condition(conditions, operators);
    }
    
    private AST.Action parseAction() {
        // Parse different types of actions (assignments, responses, etc.)
        if (match(Token.Type.IDENTIFIER)) {
            Token name = previous();
            
            if (match(Token.Type.RESPONDS)) {
                String text = consume(Token.Type.STRING, "Expected response text.").getLexeme();
                return new AST.Action("response", new AST.Response(name.getLexeme(), text));
            } else if (match(Token.Type.EQUALS)) {
                AST.Expression value = expression();
                return new AST.Action("assignment", new Object[] {name.getLexeme(), value});
            }
        }
        
        // Error handling
        error(peek(), "Expected action.");
        synchronize();
        return null;
    }
    
    private AST.Expression expression() {
        // Simple expression parsing for now
        if (match(Token.Type.STRING, Token.Type.NUMBER)) {
            return new AST.Expression(previous().getLiteral());
        } else if (match(Token.Type.IDENTIFIER)) {
            return new AST.Expression(previous().getLexeme());
        }
        
        // Function calls, complex expressions, etc. would go here
        
        // Error handling
        error(peek(), "Expected expression.");
        synchronize();
        return null;
    }
    
    // Helper methods for the parser
    
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
        
        error(peek(), message);
        return null;
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
    
    private void error(Token token, String message) {
        // Error reporting
        System.err.println("Error at line " + token.getLine() + ": " + message);
    }
    
    private void synchronize() {
        advance();
        
        while (!isAtEnd()) {
            if (previous().getType() == Token.Type.SEMICOLON) return;
            
            switch (peek().getType()) {
                case FACT:
                case RULE:
                case SCENE:
                    return;
            }
            
            advance();
        }
    }
}