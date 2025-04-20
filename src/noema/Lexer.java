package noema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lexical analyzer (Lexer) for the Noema language.
 * Converts source code strings into a stream of tokens.
 */
public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, Token.Type> keywords = new HashMap<>();

    static {
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

            case '=':
                addToken(match('=') ? Token.Type.EQUALS : Token.Type.EQUALS);
                break;

            case '!':
                if (match('=')) {
                    addToken(Token.Type.NOT_EQUALS);
                } else {
                    error("Unexpected character: !");
                }
                break;

            case '>': addToken(Token.Type.GREATER_THAN); break;
            case '<': addToken(Token.Type.LESS_THAN); break;

            case '"': string(); break;

            case '/':
                if (match('/')) {
                    // Line comment
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    error("Unexpected character: /");
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                break; // Ignore whitespace

            case '\n':
                line++;
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error("Unexpected character: " + c);
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        Token.Type type = keywords.getOrDefault(text, Token.Type.IDENTIFIER);
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // Consume '.'
            while (isDigit(peek())) advance();
        }

        double value = Double.parseDouble(source.substring(start, current));
        addToken(Token.Type.NUMBER, value);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            error("Unterminated string.");
            return;
        }

        advance(); // Consume closing "
        String value = source.substring(start + 1, current - 1);
        addToken(Token.Type.STRING, value);
    }

    // --- Helpers ---

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return Character.isDigit(c);
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

    private void error(String message) {
        System.err.println("[Line " + line + "] " + message);
    }
}
