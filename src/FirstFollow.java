package src;

import java.util.*;

public class FirstFollow {
    private Grammar grammar;
    public Map<String, Set<String>> firstSets;
    public Map<String, Set<String>> followSets;

    public FirstFollow(Grammar grammar) {
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
        
        for (String nt : grammar.nonTerminals) {
            firstSets.put(nt, new HashSet<>());
            followSets.put(nt, new HashSet<>());
        }
    }

    public void computeFirstSets() {
        boolean changed;
        do {
            changed = false;
            for (String nt : grammar.nonTerminals) {
                for (List<String> prod : grammar.rules.get(nt)) {
                    int beforeSize = firstSets.get(nt).size();
                    computeFirstForProduction(nt, prod);
                    if (firstSets.get(nt).size() > beforeSize) {
                        changed = true;
                    }
                }
            }
        } while (changed);
    }

    private void computeFirstForProduction(String lhs, List<String> production) {
        for (String symbol : production) {
            if (symbol.equals("epsilon")) {
                firstSets.get(lhs).add("epsilon");
                break;
            } else if (grammar.terminals.contains(symbol)) {
                firstSets.get(lhs).add(symbol);
                break;
            } else if (grammar.nonTerminals.contains(symbol)) {
                Set<String> symbolFirst = firstSets.get(symbol);
                for (String f : symbolFirst) {
                    if (!f.equals("epsilon")) firstSets.get(lhs).add(f);
                }
                if (!symbolFirst.contains("epsilon")) {
                    break; 
                }
            }
            // If we reach the end and everything had epsilon, add epsilon to LHS
            if (symbol.equals(production.get(production.size() - 1)) && firstSets.get(symbol).contains("epsilon")) {
                firstSets.get(lhs).add("epsilon");
            }
        }
    }

    public void computeFollowSets() {
        followSets.get(grammar.startSymbol).add("$"); // Start symbol always gets $

        boolean changed;
        do {
            changed = false;
            for (String nt : grammar.nonTerminals) {
                for (List<String> prod : grammar.rules.get(nt)) {
                    for (int i = 0; i < prod.size(); i++) {
                        String symbol = prod.get(i);
                        if (!grammar.nonTerminals.contains(symbol)) continue;

                        int beforeSize = followSets.get(symbol).size();

                        // Look at what comes after this symbol
                        boolean everythingEpsilon = true;
                        for (int j = i + 1; j < prod.size(); j++) {
                            String nextSymbol = prod.get(j);
                            
                            if (grammar.terminals.contains(nextSymbol)) {
                                followSets.get(symbol).add(nextSymbol);
                                everythingEpsilon = false;
                                break;
                            } else if (grammar.nonTerminals.contains(nextSymbol)) {
                                Set<String> nextFirst = firstSets.get(nextSymbol);
                                for (String f : nextFirst) {
                                    if (!f.equals("epsilon")) followSets.get(symbol).add(f);
                                }
                                if (!nextFirst.contains("epsilon")) {
                                    everythingEpsilon = false;
                                    break;
                                }
                            }
                        }

                        // If everything after was epsilon (or there was nothing after), add LHS Follow
                        if (everythingEpsilon && !symbol.equals(nt)) {
                            followSets.get(symbol).addAll(followSets.get(nt));
                        }

                        if (followSets.get(symbol).size() > beforeSize) {
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);
    }

    public void printSets() {
        System.out.println("--- FIRST Sets ---");
        for (String nt : grammar.nonTerminals) {
            System.out.println("FIRST(" + nt + ") = " + firstSets.get(nt));
        }
        System.out.println("\n--- FOLLOW Sets ---");
        for (String nt : grammar.nonTerminals) {
            System.out.println("FOLLOW(" + nt + ") = " + followSets.get(nt));
        }
        System.out.println("-------------------\n");
    }
}