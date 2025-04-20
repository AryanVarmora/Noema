package noema.tests;

import noema.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class InterpreterTests {

    @Test
    public void testRuleExecution() {
        String source = """
            fact score(60)
            rule pass if score > 50 {
              result = "pass"
            }
            """;

        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.scanTokens());
        Interpreter interpreter = new Interpreter();

        interpreter.interpret(parser.parse());
        interpreter.evaluateRules();  // ✅ Executes rules after parsing

        Object result = interpreter.getVariable("result");
        assertEquals("pass", result);
    }
}
