package noema;

import java.util.*;

public class Interpreter implements AST.Visitor<Object> {

    private final Map<String, List<Object[]>> facts = new HashMap<>();
    private final Map<String, AST.Rule> rules = new HashMap<>();
    private final Map<String, AST.Scene> scenes = new HashMap<>();
    private final Map<String, Map<String, Object>> npcStates = new HashMap<>();
    private final Map<String, Object> globalVariables = new HashMap<>();
    private AST.Scene currentScene = null;

    public void interpret(AST.Program program) {
        for (AST.Node stmt : program.statements) execute(stmt);
    }

    public void setScene(String name) {
        currentScene = scenes.getOrDefault(name, null);
        if (currentScene == null) System.err.println("Scene not found: " + name);
    }

    public String processInput(String input) {
        if (currentScene == null) return "No active scene.";
        for (AST.When trigger : currentScene.triggers) {
            if (evaluateCondition(trigger.condition, input)) {
                StringBuilder response = new StringBuilder();
                for (AST.Action action : trigger.actions) {
                    Object result = executeAction(action);
                    if (result instanceof String) response.append(result).append("\n");
                }
                return response.toString().trim();
            }
        }
        return "I don't know how to respond to that.";
    }

    private Object execute(AST.Node node) {
        return node.accept(this);
    }

    private Object executeAction(AST.Action action) {
        switch (action.type) {
            case "response":
                AST.Response r = (AST.Response) action.value;
                return r.character + ": " + r.text;
            case "assignment":
                Object[] parts = (Object[]) action.value;
                String target = (String) parts[0];
                Object value = evaluate((AST.Expression) parts[1]);
                if (target.startsWith("mood(")) {
                    String npc = target.substring(5, target.length() - 1);
                    setNPCState(npc, "mood", value);
                } else {
                    globalVariables.put(target, value);
                }
                return null;
            default:
                System.err.println("Unknown action: " + action.type);
                return null;
        }
    }

    private boolean evaluateCondition(AST.Condition cond, String input) {
        if (cond.conditions.size() == 1) return evaluateSimpleCondition(cond.conditions.get(0), input);
        boolean result = evaluateSimpleCondition(cond.conditions.get(0), input);
        for (int i = 0; i < cond.operators.size(); i++) {
            String op = cond.operators.get(i);
            boolean next = evaluateSimpleCondition(cond.conditions.get(i + 1), input);
            if (op.equals("and")) result = result && next;
            else if (op.equals("or")) result = result || next;
        }
        return result;
    }

    private boolean evaluateSimpleCondition(AST.Expression expr, String input) {
        Object value = evaluate(expr);
        System.out.println("[Debug] Evaluating condition: " + value);
        if (value instanceof String) {
            String s = (String) value;
            if (s.contains(">")) {
                String[] parts = s.split(">");
                return Double.parseDouble(globalVariables.get(parts[0].trim()).toString()) > Double.parseDouble(parts[1].trim());
            } else if (s.contains("<")) {
                String[] parts = s.split("<");
                return Double.parseDouble(globalVariables.get(parts[0].trim()).toString()) < Double.parseDouble(parts[1].trim());
            } else if (s.contains("==")) {
                String[] parts = s.split("==");
                return globalVariables.get(parts[0].trim()).toString().equals(parts[1].trim());
            } else if (s.contains("!=")) {
                String[] parts = s.split("!=");
                return !globalVariables.get(parts[0].trim()).toString().equals(parts[1].trim());
            }
        }
        return value.equals(input);
    }

    private Object evaluate(AST.Expression expr) {
        if (expr.value instanceof String) {
            String s = (String) expr.value;
            if (s.contains("(") && s.endsWith(")")) {
                String func = s.substring(0, s.indexOf("("));
                String arg = s.substring(s.indexOf("(") + 1, s.length() - 1);
                if (func.equals("mood")) return getNPCState(arg, "mood");
            }
            return s;
        }
        return expr.value;
    }

    private Object getNPCState(String npc, String key) {
        npcStates.putIfAbsent(npc, new HashMap<>());
        return npcStates.get(npc).getOrDefault(key, "neutral");
    }

    private void setNPCState(String npc, String key, Object value) {
        npcStates.putIfAbsent(npc, new HashMap<>());
        npcStates.get(npc).put(key, value);
    }

    public Object getVariable(String name) {
        return globalVariables.get(name);
    }

    @Override public Object visitProgramNode(AST.Program node) {
        for (AST.Node s : node.statements) execute(s);
        return null;
    }

    @Override public Object visitFactNode(AST.Fact node) {
        List<Object> args = new ArrayList<>();
        for (AST.Expression e : node.arguments) {
            Object val = evaluate(e);
            args.add(val);
        }
        facts.putIfAbsent(node.predicate, new ArrayList<>());
        facts.get(node.predicate).add(args.toArray());
        if (args.size() == 1) {
            globalVariables.put(node.predicate, args.get(0));
        }
        return null;
    }

    @Override public Object visitRuleNode(AST.Rule node) {
        rules.put(node.name, node);
        return null;
    }

    @Override public Object visitSceneNode(AST.Scene node) {
        scenes.put(node.name, node);
        for (AST.NPC npc : node.npcs) {
            npcStates.putIfAbsent(npc.name, new HashMap<>());
            if (npc.mood != null) npcStates.get(npc.name).put("mood", evaluate(npc.mood));
        }
        return null;
    }

    @Override public Object visitNPCNode(AST.NPC node) { return null; }
    @Override public Object visitWhenNode(AST.When node) { return null; }
    @Override public Object visitResponseNode(AST.Response node) {
        return node.character + ": " + node.text;
    }
    @Override public Object visitExpressionNode(AST.Expression node) { return evaluate(node); }
    @Override public Object visitConditionNode(AST.Condition node) { return null; }
    @Override public Object visitActionNode(AST.Action node) { return executeAction(node); }
    @Override public Object visitTemporalConditionNode(AST.TemporalCondition node) { return null; }


    public Set<String> getVariableNames() {
        return globalVariables.keySet();
    }
    
    public void evaluateRules() {
        for (AST.Rule rule : rules.values()) {
            if (evaluateCondition(rule.condition, "")) {
                for (AST.Action action : rule.actions) {
                    executeAction(action);
                }
            }
        }
    }
    

}
