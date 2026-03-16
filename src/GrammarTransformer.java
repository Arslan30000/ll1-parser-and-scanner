package src;

import java.util.*;

public class GrammarTransformer {

    // --- 1. LEFT FACTORING ---
    public static void applyLeftFactoring(Grammar grammar) {
        boolean changed;
        do {
            changed = false;
            List<String> currentNonTerminals = new ArrayList<>(grammar.nonTerminals);
            
            for (String nt : currentNonTerminals) {
                List<List<String>> rules = grammar.rules.get(nt);
                if (rules == null || rules.size() <= 1) continue;

                // Find the longest common prefix among the rules
                List<String> longestPrefix = new ArrayList<>();
                List<List<String>> matchingRules = new ArrayList<>();
                
                for (int i = 0; i < rules.size(); i++) {
                    for (int j = i + 1; j < rules.size(); j++) {
                        List<String> prefix = getCommonPrefix(rules.get(i), rules.get(j));
                        if (prefix.size() > longestPrefix.size()) {
                            longestPrefix = prefix;
                        }
                    }
                }

                if (!longestPrefix.isEmpty()) {
                    changed = true;
                    String newNt = nt + "prime"; // Create new Non-Terminal
                    while (grammar.nonTerminals.contains(newNt)) newNt += "'"; 
                    
                    grammar.nonTerminals.add(newNt);
                    grammar.rules.put(newNt, new ArrayList<>());

                    List<List<String>> newRulesForNt = new ArrayList<>();
                    
                    // Group rules by whether they start with the common prefix
                    for (List<String> rule : rules) {
                        if (startsWith(rule, longestPrefix)) {
                            List<String> suffix = new ArrayList<>(rule.subList(longestPrefix.size(), rule.size()));
                            if (suffix.isEmpty()) suffix.add("epsilon");
                            grammar.rules.get(newNt).add(suffix);
                        } else {
                            newRulesForNt.add(rule);
                        }
                    }

                    // Add the factored rule: A -> prefix A'
                    List<String> factoredRule = new ArrayList<>(longestPrefix);
                    factoredRule.add(newNt);
                    newRulesForNt.add(factoredRule);

                    grammar.rules.put(nt, newRulesForNt);
                    break; // Restart the process to catch multiple factorings
                }
            }
        } while (changed);
    }

    private static List<String> getCommonPrefix(List<String> r1, List<String> r2) {
        List<String> prefix = new ArrayList<>();
        int minLen = Math.min(r1.size(), r2.size());
        for (int i = 0; i < minLen; i++) {
            if (r1.get(i).equals(r2.get(i))) prefix.add(r1.get(i));
            else break;
        }
        return prefix;
    }

    private static boolean startsWith(List<String> rule, List<String> prefix) {
        if (rule.size() < prefix.size()) return false;
        for (int i = 0; i < prefix.size(); i++) {
            if (!rule.get(i).equals(prefix.get(i))) return false;
        }
        return true;
    }


    // --- 2. LEFT RECURSION REMOVAL (Immediate & Indirect) ---
    public static void eliminateLeftRecursion(Grammar grammar) {
        List<String> nonTerminals = new ArrayList<>(grammar.nonTerminals);
        
        // Loop over non-terminals (Ai)
        for (int i = 0; i < nonTerminals.size(); i++) {
            String ai = nonTerminals.get(i);
            
            // Indirect recursion removal: substitute Aj into Ai
            for (int j = 0; j < i; j++) {
                String aj = nonTerminals.get(j);
                List<List<String>> aiRules = new ArrayList<>(grammar.rules.get(ai));
                List<List<String>> newAiRules = new ArrayList<>();
                
                boolean substituted = false;
                for (List<String> rule : aiRules) {
                    if (rule.get(0).equals(aj)) { // Indirect recursion found!
                        substituted = true;
                        List<String> suffix = rule.subList(1, rule.size());
                        for (List<String> ajRule : grammar.rules.get(aj)) {
                            List<String> combined = new ArrayList<>();
                            if (!ajRule.get(0).equals("epsilon")) combined.addAll(ajRule);
                            combined.addAll(suffix);
                            if (combined.isEmpty()) combined.add("epsilon");
                            newAiRules.add(combined);
                        }
                    } else {
                        newAiRules.add(rule);
                    }
                }
                if (substituted) grammar.rules.put(ai, newAiRules);
            }
            
            // Immediate recursion removal for Ai
            eliminateImmediateLeftRecursion(grammar, ai);
        }
    }

    private static void eliminateImmediateLeftRecursion(Grammar grammar, String nt) {
        List<List<String>> rules = grammar.rules.get(nt);
        List<List<String>> alphas = new ArrayList<>(); // recursive parts
        List<List<String>> betas = new ArrayList<>();  // non-recursive parts
        
        for (List<String> rule : rules) {
            if (rule.get(0).equals(nt)) {
                alphas.add(new ArrayList<>(rule.subList(1, rule.size())));
            } else {
                betas.add(rule);
            }
        }
        
        if (alphas.isEmpty()) return; // No immediate left recursion
        
        String newNt = nt + "prime";
        while (grammar.nonTerminals.contains(newNt)) newNt += "'";
        
        grammar.nonTerminals.add(newNt);
        grammar.rules.put(newNt, new ArrayList<>());
        
        List<List<String>> newRulesForNt = new ArrayList<>();
        
        // A -> beta A'
        if (betas.isEmpty()) {
            newRulesForNt.add(Arrays.asList(newNt));
        } else {
            for (List<String> beta : betas) {
                List<String> newRule = new ArrayList<>();
                if (!beta.get(0).equals("epsilon")) newRule.addAll(beta);
                newRule.add(newNt);
                newRulesForNt.add(newRule);
            }
        }
        grammar.rules.put(nt, newRulesForNt);
        
        // A' -> alpha A' | epsilon
        for (List<String> alpha : alphas) {
            List<String> newRule = new ArrayList<>(alpha);
            newRule.add(newNt);
            grammar.rules.get(newNt).add(newRule);
        }
        grammar.rules.get(newNt).add(Arrays.asList("epsilon"));
    }
}