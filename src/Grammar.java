package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Grammar {
    // Maps a Non-Terminal to a list of its productions (each production is a list of symbols)
    public Map<String, List<List<String>>> rules;
    public String startSymbol;
    public Set<String> nonTerminals;
    public Set<String> terminals;

    public Grammar() {
        rules = new LinkedHashMap<>();
        nonTerminals = new LinkedHashSet<>();
        terminals = new LinkedHashSet<>();
    }

    public void loadFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        boolean isFirst = true;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("->");
            if (parts.length != 2) continue;

            String lhs = parts[0].trim();
            nonTerminals.add(lhs);

            if (isFirst) {
                startSymbol = lhs;
                isFirst = false;
            }

            String[] rhsAlternatives = parts[1].split("\\|");
            rules.putIfAbsent(lhs, new ArrayList<>());

            for (String rhs : rhsAlternatives) {
                List<String> production = new ArrayList<>();
                for (String symbol : rhs.trim().split("\\s+")) {
                    production.add(symbol);
                }
                rules.get(lhs).add(production);
            }
        }
        reader.close();
        extractTerminals();
    }

    private void extractTerminals() {
        for (List<List<String>> productions : rules.values()) {
            for (List<String> prod : productions) {
                for (String symbol : prod) {
                    if (!nonTerminals.contains(symbol) && !symbol.equals("epsilon")) {
                        terminals.add(symbol);
                    }
                }
            }
        }
    }

    public void printGrammar() {
        System.out.println("--- Context Free Grammar ---");
        for (String nt : rules.keySet()) {
            System.out.print(nt + " -> ");
            List<List<String>> prods = rules.get(nt);
            for (int i = 0; i < prods.size(); i++) {
                System.out.print(String.join(" ", prods.get(i)));
                if (i < prods.size() - 1) System.out.print(" | ");
            }
            System.out.println();
        }
        System.out.println("----------------------------\n");
    }
}