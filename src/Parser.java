package src;

import java.util.*;

public class Parser {
    private Grammar grammar;
    private FirstFollow ff;
    public Map<String, Map<String, List<String>>> parsingTable;
    private StringBuilder traceBuilder; 

    public Parser(Grammar grammar, FirstFollow ff) {
        this.grammar = grammar;
        this.ff = ff;
        this.parsingTable = new HashMap<>();
        buildParsingTable();
    }

    private void buildParsingTable() {
        for (String nt : grammar.nonTerminals) parsingTable.put(nt, new HashMap<>());

        for (String nt : grammar.nonTerminals) {
            for (List<String> prod : grammar.rules.get(nt)) {
                Set<String> firstOfProd = getFirstOfSequence(prod);

                for (String terminal : firstOfProd) {
                    if (!terminal.equals("epsilon")) {
                        parsingTable.get(nt).put(terminal, prod);
                    }
                }
                if (firstOfProd.contains("epsilon")) {
                    for (String terminal : ff.followSets.get(nt)) {
                        parsingTable.get(nt).put(terminal, prod);
                    }
                }
            }
        }
    }

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
                for (String f : symbolFirst) if (!f.equals("epsilon")) firstSet.add(f);
                if (!symbolFirst.contains("epsilon")) {
                    allHaveEpsilon = false;
                    break;
                }
            }
        }
        if (allHaveEpsilon) firstSet.add("epsilon");
        return firstSet;
    }

    public String getTableString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- LL(1) Parsing Table ---\n");
        for (String nt : parsingTable.keySet()) {
            Map<String, List<String>> row = parsingTable.get(nt);
            for (String terminal : row.keySet()) {
                sb.append("M[").append(nt).append(", ").append(terminal).append("] = ")
                  .append(nt).append(" -> ").append(String.join(" ", row.get(terminal))).append("\n");
            }
        }
        sb.append("---------------------------\n");
        return sb.toString();
    }

    public Tree parse(String[] inputTokens, ErrorHandler errorHandler) {
        traceBuilder = new StringBuilder();
        traceBuilder.append("--- Parsing Input: ").append(String.join(" ", inputTokens)).append(" ---\n");
        
        Stack<Tree> stack = new Stack<>();
        Tree endMarker = new Tree("$");
        Tree root = new Tree(grammar.startSymbol);
        
        stack.push(endMarker);
        stack.push(root);

        List<String> input = new ArrayList<>(Arrays.asList(inputTokens));
        input.add("$");

        int ip = 0;
        traceBuilder.append(String.format("%-40s %-30s %-30s%n", "STACK", "INPUT", "ACTION"));
        
        boolean parseSuccess = true;

        while (!stack.isEmpty()) {
            Tree topNode = stack.peek();
            String top = topNode.symbol;
            String currentInput = input.get(ip);

            String stackStr = stack.toString();
            String inputStr = String.join(" ", input.subList(ip, input.size()));
            traceBuilder.append(String.format("%-40s %-30s ", stackStr, inputStr));

            if (top.equals(currentInput)) {
                if (top.equals("$")) {
                    traceBuilder.append("Accept!\n");
                    break;
                } else {
                    traceBuilder.append("Match ").append(top).append("\n");
                    stack.pop();
                    ip++;
                }
            } else if (grammar.terminals.contains(top) || top.equals("$")) {
                String err = "Error: Expected " + top + " but found " + currentInput + ". (Popping stack)";
                traceBuilder.append(err).append("\n");
                errorHandler.reportError(err);
                stack.pop();
                parseSuccess = false;
            } else {
                if (parsingTable.containsKey(top) && parsingTable.get(top).containsKey(currentInput)) {
                    List<String> prod = parsingTable.get(top).get(currentInput);
                    traceBuilder.append("Output ").append(top).append(" -> ").append(String.join(" ", prod)).append("\n");
                    
                    stack.pop();
                    if (!prod.get(0).equals("epsilon")) {
                        List<Tree> newNodes = new ArrayList<>();
                        for (String s : prod) newNodes.add(new Tree(s));
                        topNode.children.addAll(newNodes);
                        for (int i = newNodes.size() - 1; i >= 0; i--) stack.push(newNodes.get(i));
                    } else {
                        topNode.children.add(new Tree("epsilon"));
                    }
                } else {
                    String err = "Error: Unexpected token " + currentInput + ". (Skipping input)";
                    traceBuilder.append(err).append("\n");
                    errorHandler.reportError(err);
                    ip++;
                    parseSuccess = false;
                    if (ip >= input.size()) break;
                }
            }
        }
        traceBuilder.append("----------------------------------------\n");
        return parseSuccess ? root : null; // Returns null if panic mode was triggered
    }

    public String getLastTrace() {
        return traceBuilder != null ? traceBuilder.toString() : "";
    }
}