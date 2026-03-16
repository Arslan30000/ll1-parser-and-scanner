package src;

import java.util.*;

public class LL1Parser {
    private Grammar grammar;
    private FirstFollow ff;
    
    // The Parsing Table: Map<NonTerminal, Map<Terminal, Production>>
    public Map<String, Map<String, List<String>>> parsingTable;

    public LL1Parser(Grammar grammar, FirstFollow ff) {
        this.grammar = grammar;
        this.ff = ff;
        this.parsingTable = new HashMap<>();
        buildParsingTable();
    }

    private void buildParsingTable() {
        for (String nt : grammar.nonTerminals) {
            parsingTable.put(nt, new HashMap<>());
        }

        for (String nt : grammar.nonTerminals) {
            for (List<String> prod : grammar.rules.get(nt)) {
                Set<String> firstOfProd = getFirstOfSequence(prod);

                // Rule 1: For every terminal a in FIRST(A -> alpha), add A -> alpha to M[A, a]
                for (String terminal : firstOfProd) {
                    if (!terminal.equals("epsilon")) {
                        parsingTable.get(nt).put(terminal, prod);
                    }
                }

                // Rule 2: If epsilon is in FIRST(A -> alpha), add A -> alpha to M[A, b] for each b in FOLLOW(A)
                if (firstOfProd.contains("epsilon")) {
                    for (String terminal : ff.followSets.get(nt)) {
                        parsingTable.get(nt).put(terminal, prod);
                    }
                }
            }
        }
    }

    // Helper method to get FIRST set of a sequence of symbols (like the RHS of a production)
    private Set<String> getFirstOfSequence(List<String> sequence) {
        Set<String> firstSet = new HashSet<>();
        boolean allHaveEpsilon = true;

        for (String symbol : sequence) {
            if (symbol.equals("epsilon")) {
                firstSet.add("epsilon");
                break;
            } else if (grammar.terminals.contains(symbol)) {
                firstSet.add(symbol);
                allHaveEpsilon = false;
                break;
            } else if (grammar.nonTerminals.contains(symbol)) {
                Set<String> symbolFirst = ff.firstSets.get(symbol);
                for (String f : symbolFirst) {
                    if (!f.equals("epsilon")) firstSet.add(f);
                }
                if (!symbolFirst.contains("epsilon")) {
                    allHaveEpsilon = false;
                    break;
                }
            }
        }
        if (allHaveEpsilon) firstSet.add("epsilon");
        return firstSet;
    }

    public void printTable() {
        System.out.println("--- LL(1) Parsing Table ---");
        for (String nt : parsingTable.keySet()) {
            Map<String, List<String>> row = parsingTable.get(nt);
            for (String terminal : row.keySet()) {
                System.out.println("M[" + nt + ", " + terminal + "] = " + nt + " -> " + String.join(" ", row.get(terminal)));
            }
        }
        System.out.println("---------------------------\n");
    }

    // The Stack-Based Parsing Algorithm with Panic Mode Error Recovery
    public void parse(String[] inputTokens) {
        System.out.println("--- Parsing Input: " + String.join(" ", inputTokens) + " ---");
        
        Stack<String> stack = new Stack<>();
        stack.push("$");
        stack.push(grammar.startSymbol);

        List<String> input = new ArrayList<>(Arrays.asList(inputTokens));
        input.add("$"); // Append End of Input marker

        int ip = 0; // Input Pointer

        System.out.printf("%-20s %-20s %-20s%n", "STACK", "INPUT", "ACTION");
        
        while (!stack.isEmpty()) {
            String top = stack.peek();
            String currentInput = input.get(ip);

            // Print current state
            String stackStr = String.join(" ", stack);
            String inputStr = String.join(" ", input.subList(ip, input.size()));
            System.out.printf("%-20s %-20s ", stackStr, inputStr);

            if (top.equals(currentInput)) {
                if (top.equals("$")) {
                    System.out.println("Accept!");
                    break;
                } else {
                    System.out.println("Match " + top);
                    stack.pop();
                    ip++;
                }
            } else if (grammar.terminals.contains(top) || top.equals("$")) {
                // Error: Terminal on stack doesn't match input
                System.out.println("Error: Expected " + top + " but found " + currentInput + ". (Popping stack)");
                stack.pop(); // Panic Mode Recovery: Pop the mismatched terminal
            } else {
                // Non-Terminal on stack. Look up in parsing table.
                if (parsingTable.containsKey(top) && parsingTable.get(top).containsKey(currentInput)) {
                    List<String> prod = parsingTable.get(top).get(currentInput);
                    System.out.println("Output " + top + " -> " + String.join(" ", prod));
                    
                    stack.pop();
                    // Push production to stack in reverse order
                    if (!prod.get(0).equals("epsilon")) {
                        for (int i = prod.size() - 1; i >= 0; i--) {
                            stack.push(prod.get(i));
                        }
                    }
                } else {
                    // Error: Blank entry in parsing table
                    System.out.println("Error: Unexpected token " + currentInput + ". (Skipping input)");
                    ip++; // Panic Mode Recovery: Skip the offending input token
                    
                    if (ip >= input.size()) {
                        System.out.println("Failed to parse string.");
                        break;
                    }
                }
            }
        }
        System.out.println("----------------------------------------\n");
    }
}