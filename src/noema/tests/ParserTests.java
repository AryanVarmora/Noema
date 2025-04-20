package noema.tests;

import noema.*;
import noema.AST.*;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Noema language parser.
 */
public class ParserTests {


    public void testFactParsing() {
        String source = "fact person(\"Alice\")";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);
        Program program = parser.parse();

        assertEquals(1, program.statements.size());
        assertTrue(program.statements.get(0) instanceof Fact);

        Fact fact = (Fact) program.statements.get(0);
        assertEquals("person", fact.predicate);
        assertEquals(1, fact.arguments.size());
        assertEquals("Alice", fact.arguments.get(0).value);
    }


    public void testRuleParsing() {
        String source = "rule greet if \"happy\" = \"happy\" { mood responds \"Hello!\" }";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);
        Program program = parser.parse();

        assertEquals(1, program.statements.size());
        assertTrue(program.statements.get(0) instanceof Rule);

        Rule rule = (Rule) program.statements.get(0);
        assertEquals("greet", rule.name);
        assertEquals(1, rule.condition.conditions.size());
        assertEquals(1, rule.actions.size());

        Action action = rule.actions.get(0);
        assertEquals("response", action.type);
    }


    public void testSceneParsing() {
        String source = "scene \"Intro\" { npc \"Bob\" feels \"happy\" when player says \"hi\" { mood responds \"Hey!\" } }";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);
        Program program = parser.parse();

        assertEquals(1, program.statements.size());
        assertTrue(program.statements.get(0) instanceof Scene);

        Scene scene = (Scene) program.statements.get(0);
        assertEquals("Intro", scene.name);
        assertEquals(1, scene.npcs.size());
        assertEquals(1, scene.triggers.size());
    }
}
