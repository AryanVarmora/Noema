package noema.tests;

import noema.Lexer;
import noema.Token;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class LexerTests {

    @Test
    public void testFactTokenization() {
        String source = "fact person(\"Alice\")";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        assertEquals(6, tokens.size()); // Including EOF
        assertEquals(Token.Type.FACT, tokens.get(0).getType());
        assertEquals(Token.Type.IDENTIFIER, tokens.get(1).getType());
        assertEquals("person", tokens.get(1).getLexeme());
        assertEquals(Token.Type.OPEN_PAREN, tokens.get(2).getType());
        assertEquals(Token.Type.STRING, tokens.get(3).getType());
        assertEquals("Alice", tokens.get(3).getLiteral());
        assertEquals(Token.Type.CLOSE_PAREN, tokens.get(4).getType());
        assertEquals(Token.Type.EOF, tokens.get(5).getType());
    }
}
