// ============================
// Parser.java (rewritten)
// ============================

package noema;

import java.util.ArrayList;
import java.util.List;

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
        if (match(Token.Type.FACT)) return factDeclaration();
        if (match(Token.Type.RULE)) return ruleDeclaration();
        if (match(Token.Type.SCENE)) return sceneDeclaration();

        error(peek(), "Expected declaration.");
        synchronize();
        return null;
    }

    private AST.Fact factDeclaration() {
        String predicate = consume(Token.Type.IDENTIFIER, "Expected fact predicate.").getLexeme();
        consume(Token.Type.OPEN_PAREN, "Expected '(' after predicate.");
        List<AST.Expression> args = new ArrayList<>();
        if (!check(Token.Type.CLOSE_PAREN)) {
            do { args.add(expression()); } while (match(Token.Type.COMMA));
        }
        consume(Token.Type.CLOSE_PAREN, "Expected ')' after arguments.");
        return new AST.Fact(predicate, args);
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
            if (match(Token.Type.NPC)) npcs.add(parseNPC());
            else if (match(Token.Type.WHEN)) triggers.add(parseTrigger());
            else {
                error(peek(), "Expected NPC or trigger.");
                synchronize();
            }
        }
        consume(Token.Type.CLOSE_BRACE, "Expected '}' after scene body.");
        return new AST.Scene(name, npcs, triggers);
    }

    private AST.NPC parseNPC() {
        String name = consume(Token.Type.STRING, "Expected NPC name.").getLexeme();
        consume(Token.Type.FEELS, "Expected 'feels'.");
        AST.Expression mood = expression();
        return new AST.NPC(name, mood);
    }

    private AST.When parseTrigger() {
        consume(Token.Type.PLAYER, "Expected 'player'.");
        consume(Token.Type.SAYS, "Expected 'says'.");
        AST.Condition condition = parseCondition();
        consume(Token.Type.OPEN_BRACE, "Expected '{'.");
        List<AST.Action> actions = new ArrayList<>();
        while (!check(Token.Type.CLOSE_BRACE) && !isAtEnd()) {
            actions.add(parseAction());
        }
        consume(Token.Type.CLOSE_BRACE, "Expected '}' after trigger.");
        return new AST.When(condition, actions);
    }

    private AST.Condition parseCondition() {
        List<AST.Expression> expressions = new ArrayList<>();
        List<String> operators = new ArrayList<>();

        expressions.add(expression());
        while (match(Token.Type.AND, Token.Type.OR)) {
            operators.add(previous().getLexeme());
            expressions.add(expression());
        }

        return new AST.Condition(expressions, operators);
    }

    private AST.Action parseAction() {
        if (match(Token.Type.IDENTIFIER)) {
            Token name = previous();
            if (match(Token.Type.RESPONDS)) {
                String text = consume(Token.Type.STRING, "Expected response.").getLexeme();
                return new AST.Action("response", new AST.Response(name.getLexeme(), text));
            } else if (match(Token.Type.EQUALS)) {
                AST.Expression value = expression();
                return new AST.Action("assignment", new Object[] { name.getLexeme(), value });
            }
        }
        error(peek(), "Expected action.");
        synchronize();
        return null;
    }

    private AST.Expression expression() {
        AST.Expression left = simpleExpression();
        if (match(Token.Type.EQUALS, Token.Type.NOT_EQUALS, Token.Type.GREATER_THAN, Token.Type.LESS_THAN)) {
            String op = previous().getLexeme();
            AST.Expression right = simpleExpression();
            return new AST.Expression(left.value + " " + op + " " + right.value);
        }
        return left;
    }

    private AST.Expression simpleExpression() {
        if (match(Token.Type.STRING, Token.Type.NUMBER)) return new AST.Expression(previous().getLiteral());
        if (match(Token.Type.IDENTIFIER)) return new AST.Expression(previous().getLexeme());
        error(peek(), "Expected expression.");
        synchronize();
        return null;
    }

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
        return !isAtEnd() && peek().getType() == type;
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
        System.err.println("[Line " + token.getLine() + "] Error: " + message);
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().getType() == Token.Type.SEMICOLON) return;
            if (check(Token.Type.FACT) || check(Token.Type.RULE) || check(Token.Type.SCENE)) return;
            advance();
        }
    }
}