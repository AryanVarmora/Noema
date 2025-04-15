package NOEMA;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Syntax Tree nodes for the Noema language
 */
public class AST {
    
    // Base interface for all AST nodes
    public interface Node {
        <R> R accept(Visitor<R> visitor);
    }
    
    // Visitor pattern interface
    public interface Visitor<R> {
        R visitProgramNode(Program node);
        R visitFactNode(Fact node);
        R visitRuleNode(Rule node);
        R visitSceneNode(Scene node);
        R visitNPCNode(NPC node);
        R visitWhenNode(When node);
        R visitResponseNode(Response node);
        R visitExpressionNode(Expression node);
        R visitConditionNode(Condition node);
        R visitActionNode(Action node);
    }
    
    // Root node for a complete program
    public static class Program implements Node {
        final List<Node> statements = new ArrayList<>();
        
        public void addStatement(Node statement) {
            statements.add(statement);
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitProgramNode(this);
        }
    }
    
    // Fact declaration
    public static class Fact implements Node {
        final String predicate;
        final List<Expression> arguments;
        
        public Fact(String predicate, List<Expression> arguments) {
            this.predicate = predicate;
            this.arguments = arguments;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFactNode(this);
        }
    }
    
    // Rule definition
    public static class Rule implements Node {
        final String name;
        final Condition condition;
        final List<Action> actions;
        
        public Rule(String name, Condition condition, List<Action> actions) {
            this.name = name;
            this.condition = condition;
            this.actions = actions;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitRuleNode(this);
        }
    }
    
    // Scene definition
    public static class Scene implements Node {
        final String name;
        final List<NPC> npcs;
        final List<When> triggers;
        
        public Scene(String name, List<NPC> npcs, List<When> triggers) {
            this.name = name;
            this.npcs = npcs;
            this.triggers = triggers;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSceneNode(this);
        }
    }
    
    // NPC declaration in a scene
    public static class NPC implements Node {
        final String name;
        final Expression mood;
        
        public NPC(String name, Expression mood) {
            this.name = name;
            this.mood = mood;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitNPCNode(this);
        }
    }
    
    // When clause for dialogue triggers
    public static class When implements Node {
        final Condition condition;
        final List<Action> actions;
        
        public When(Condition condition, List<Action> actions) {
            this.condition = condition;
            this.actions = actions;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenNode(this);
        }
    }
    
    // Response action
    public static class Response implements Node {
        final String character;
        final String text;
        
        public Response(String character, String text) {
            this.character = character;
            this.text = text;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitResponseNode(this);
        }
    }
    
    // Expression (used in fact arguments, conditions, etc.)
    public static class Expression implements Node {
        final Object value;  // Could be a literal value or a variable reference
        
        public Expression(Object value) {
            this.value = value;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionNode(this);
        }
    }
    
    // Condition for rules and when clauses
    public static class Condition implements Node {
        final List<Expression> conditions;
        final List<String> operators;  // "and", "==", etc.
        
        public Condition(List<Expression> conditions, List<String> operators) {
            this.conditions = conditions;
            this.operators = operators;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitConditionNode(this);
        }
    }
    
    // Action for rule execution
    public static class Action implements Node {
        final String type;  // "assignment", "response", etc.
        final Object value;  // Depends on the type
        
        public Action(String type, Object value) {
            this.type = type;
            this.value = value;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitActionNode(this);
        }
    }
}