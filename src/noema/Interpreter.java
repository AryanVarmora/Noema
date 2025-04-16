package noema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interpreter for the Noema language
 */
public class Interpreter  {
    
    // Knowledge base for storing facts and rules
    private final Map<String, List<Object[]>> facts = new HashMap<>();
    private final Map<String, AST.Rule> rules = new HashMap<>();
    
    // Scene management
    private final Map<String, AST.Scene> scenes = new HashMap<>();
    private AST.Scene currentScene = null;
    
    // NPC state
    private final Map<String, Map<String, Object>> npcStates = new HashMap<>();
    
    public Interpreter() {
        // Initialize the interpreter
    }
    
    public void interpret(AST.Program program) {
        for (AST.Node statement : program.statements) {
            execute(statement);
        }
    }
    
    private void execute(AST.Node node) {
        node.accept(this);
    }
    
    // Process player input in the current scene
    public String processInput(String input) {
        if (currentScene == null) {
            return "No active scene.";
        }
        
        for (AST.When trigger : currentScene.triggers) {
            // Check if the trigger condition matches the input
            if (evaluateCondition(trigger.condition, input)) {
                StringBuilder response = new StringBuilder();
                
                // Execute all actions in the trigger
                for (AST.Action action : trigger.actions) {
                    Object result = executeAction(action);
                    if (result instanceof String) {
                        response.append(result).append("\n");
                    }
                }
                
                return response.toString().trim();
            }
        }
        
        return "I don't know how to respond to that.";
    }
    
    // Set the current active scene
    public void setScene(String sceneName) {
        if (scenes.containsKey(sceneName)) {
            currentScene = scenes.get(sceneName);
        } else {
            System.err.println("Scene not found: " + sceneName);
        }
    }
    
    // Execute a single action and return any result
    private Object executeAction(AST.Action action) {
        switch (action.type) {
            case "response":
                AST.Response response = (AST.Response) action.value;
                return response.character + ": " + response.text;
                
            case "assignment":
                Object[] assignment = (Object[]) action.value;
                String target = (String) assignment[0];
                Object value = evaluate((AST.Expression) assignment[1]);
                
                // Handle different types of assignments (mood, trust, etc.)
                if (target.startsWith("mood(")) {
                    String npc = target.substring(5, target.length() - 1);
                    setNPCState(npc, "mood", value);
                } else {
                    // Other assignments
                }
                
                return null;
                
            default:
                System.err.println("Unknown action type: " + action.type);
                return null;
        }
    }
    
    // Evaluate a condition against the current state
    private boolean evaluateCondition(AST.Condition condition, String input) {
        // Simple condition evaluation for now
        if (condition.conditions.size() == 1) {
            Object condValue = evaluate(condition.conditions.get(0));
            return condValue.equals(input);
        }
        
        // Complex conditions with AND, OR, etc.
        boolean result = evaluateSimpleCondition(condition.conditions.get(0), input);
        
        for (int i = 0; i < condition.operators.size(); i++) {
            String operator = condition.operators.get(i);
            if (operator.equals("and")) {
                result = result && evaluateSimpleCondition(condition.conditions.get(i + 1), input);
            }
            // Add other operators as needed
        }
        
        return result;
    }
    
    private boolean evaluateSimpleCondition(AST.Expression expr, String input) {
        Object value = evaluate(expr);
        return value.equals(input);
    }
    
    // Evaluate an expression to get its value
    private Object evaluate(AST.Expression expr) {
        if (expr.value instanceof String) {
            String value = (String) expr.value;
            
            // Check if this is a function call
            if (value.contains("(") && value.endsWith(")")) {
                String funcName = value.substring(0, value.indexOf("("));
                String arg = value.substring(value.indexOf("(") + 1, value.length() - 1);
                
                // Handle different functions
                if (funcName.equals("mood")) {
                    return getNPCState(arg, "mood");
                }
                // Add other functions as needed
            }
            
            return value;
        }
        
        return expr.value;
    }
    
    // Get an NPC's state value
    private Object getNPCState(String npc, String key) {
        if (!npcStates.containsKey(npc)) {
            npcStates.put(npc, new HashMap<>());
        }
        
        return npcStates.get(npc).getOrDefault(key, "neutral");
    }
    
    // Set an NPC's state value
    private void setNPCState(String npc, String key, Object value) {
        if (!npcStates.containsKey(npc)) {
            npcStates.put(npc, new HashMap<>());
        }
        
        npcStates.get(npc).put(key, value);
    }
    
    // Visitor implementation
    
    @Override
    public Object visitProgramNode(AST.Program node) {
        for (AST.Node statement : node.statements) {
            execute(statement);
        }
        return null;
    }
    
    @Override
    public Object visitFactNode(AST.Fact node) {
        List<Object> args = new ArrayList<>();
        for (AST.Expression arg : node.arguments) {
            args.add(evaluate(arg));
        }
        
        addFact(node.predicate, args.toArray());
        return null;
    }
    
    @Override
    public Object visitRuleNode(AST.Rule node) {
        rules.put(node.name, node);
        return null;
    }
    
    @Override
    public Object visitSceneNode(AST.Scene node) {
        scenes.put(node.name, node);
        
        // Initialize NPCs in the scene
        for (AST.NPC npc : node.npcs) {
            if (!npcStates.containsKey(npc.name)) {
                npcStates.put(npc.name, new HashMap<>());
            }
            
            if (npc.mood != null) {
                npcStates.get(npc.name).put("mood", evaluate(npc.mood));
            }
        }
        
        return null;
    }
    
    @Override
    public Object visitNPCNode(AST.NPC node) {
        // NPCs are handled in visitSceneNode
        return null;
    }
    
    @Override
    public Object visitWhenNode(AST.When node) {
        // When clauses are handled during input processing
        return null;
    }
    
    @Override
    public Object visitResponseNode(AST.Response node) {
        return node.character + ": " + node.text;
    }
    
    @Override
    public Object visitExpressionNode(AST.Expression node) {
        return evaluate(node);
    }
    
    @Override
    public Object visitConditionNode(AST.Condition node) {
        // Conditions are evaluated during input processing
        return null;
    }
    
    @Override
    public Object visitActionNode(AST.Action node) {
        return executeAction(node);
    }
    
    // Knowledge base operations
    
    private void addFact(String predicate, Object[] arguments) {
        if (!facts.containsKey(predicate)) {
            facts.put(predicate, new ArrayList<>());
        }
        
        facts.get(predicate).add(arguments);
    }
    
    private List<Object[]> queryFacts(String predicate) {
        return facts.getOrDefault(predicate, new ArrayList<>());
    }
}
