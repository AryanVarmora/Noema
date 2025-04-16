package noema;

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
        R visitTemporalConditionNode(TemporalCondition node);
    }
    
    // Root node for a complete program
    public static class Program implements Node {
        public final List<Node> statements = new ArrayList<>();
        
        public void addStatement(Node statement) {
            statements.add(statement);
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitProgramNode(this);
        }
    }
    
    // Expression (used in fact arguments, conditions, etc.)
    public static class Expression implements Node {
        public final Object value;  // Could be a literal value or a variable reference
        
        public Expression(Object value) {
            this.value = value;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionNode(this);
        }
    }
    
    // Fact declaration
    public static class Fact implements Node {
        public final String predicate;
        public final List<Expression> arguments;
        
        public Fact(String predicate, List<Expression> arguments) {
            this.predicate = predicate;
            this.arguments = arguments;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFactNode(this);
        }
    }
    
    // Condition for rules and when clauses
    public static class Condition implements Node {
        public final List<Expression> conditions;
        public final List<String> operators;  // "and", "==", etc.
        
        public Condition(List<Expression> conditions, List<String> operators) {
            this.conditions = conditions;
            this.operators = operators;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitConditionNode(this);
        }
    }
    
    // Rule definition
    public static class Rule implements Node {
        public final String name;
        public final Condition condition;
        public final List<Action> actions;
        
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
    
    // Action for rule execution
    public static class Action implements Node {
        public final String type;  // "assignment", "response", etc.
        public final Object value;  // Depends on the type
        
        public Action(String type, Object value) {
            this.type = type;
            this.value = value;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitActionNode(this);
        }
    }
    
    // Scene definition
    public static class Scene implements Node {
        public final String name;
        public final List<NPC> npcs;
        public final List<When> triggers;
        
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
        public final String name;
        public final Expression mood;
        
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
        public final Condition condition;
        public final List<Action> actions;
        
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
        public final String character;
        public final String text;
        
        public Response(String character, String text) {
            this.character = character;
            this.text = text;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitResponseNode(this);
        }
    }
    
    // Function call expression for more complex conditions
    public static class FunctionCall {
        public final String name;
        public final List<Expression> arguments;
        
        public FunctionCall(String name, List<Expression> arguments) {
            this.name = name;
            this.arguments = arguments;
        }
    }
    
    // Temporal condition for time-based rules
    public static class TemporalCondition implements Node {
        public final Expression event1;
        public final Expression event2;
        public final String temporalOperator; // "within", "before", "after"
        public final Expression duration;
        
        public TemporalCondition(
                Expression event1, 
                String temporalOperator,
                Expression event2,
                Expression duration) {
            this.event1 = event1;
            this.temporalOperator = temporalOperator;
            this.event2 = event2;
            this.duration = duration;
        }
        
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTemporalConditionNode(this);
        }
    }
}