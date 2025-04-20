package noema;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses complex conditional expressions in the Noema language,
 * supporting logical and comparison operators.
 */
public class ConditionParser {
    private final List<Token> tokens;
    private int current = 0;

    public ConditionParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Entry point to parse a condition.
     * Example: "mood = 'happy' and playerLevel > 5"
     */
    public AST.Condition parse() {
        AST.Condition condition = new AST.Condition(new ArrayList<>(), new ArrayList<>());

        // First expression
        condition.conditions.add(expression());

        // Loop through binary operators and parse right-hand expressions
        while (match(
                Token.Type.AND, Token.Type.OR,
                Token.Type.EQUALS, Token.Type.NOT_EQUALS,
                Token.Type.GREATER_THAN, Token.Type.LESS_THAN)) {

            String operator = previous().getLexeme();
            condition.operators.add(operator);
            condition.conditions.add(expression());
        }

        return condition;
    }

    /**
     * Parses an individual expression: literals, identifiers, or function calls.
     */
    private AST.Expression expression() {
        // Literal: string or number
        if (match(Token.Type.STRING, Token.Type.NUMBER)) {
            return new AST.Expression(previous().getLiteral());
        }

        // Identifier or function call
        if (match(Token.Type.IDENTIFIER)) {
            String name = previous().getLexeme();

            if (check(Token.Type.OPEN_PAREN)) {
                return functionCall(name);
            }

            return new AST.Expression(name);
        }

        // Parenthesized expression: (condition)
        if (match(Token.Type.OPEN_PAREN)) {
            AST.Expression grouped = new AST.Expression(parse());
            consume(Token.Type.CLOSE_PAREN, "Expected ')' after expression.");
            return grouped;
        }

        throw new ErrorReporter.RuntimeError(peek(), "Expected expression.");
    }

    /**
     * Parses a function call: name(arguments)
     */
    private AST.Expression functionCall(String name) {
        consume(Token.Type.OPEN_PAREN, "Expected '(' after function name.");

        List<AST.Expression> arguments = new ArrayList<>();
        if (!check(Token.Type.CLOSE_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(Token.Type.COMMA));
        }

        consume(Token.Type.CLOSE_PAREN, "Expected ')' after arguments.");
        return new AST.Expression(new AST.FunctionCall(name, arguments));
    }

    // --- Helper Methods ---

    private boolean match(Token.Type... types) {
        for (Token.Type type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(Token.Type type) {
        return !isAtEnd() && peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token consume(Token.Type type, String message) {
        if (check(type)) return advance();
        throw new ErrorReporter.RuntimeError(peek(), message);
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
