package src;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            new File("output").mkdirs();

            for (int i = 1; i <= 4; i++) {
                processGrammarPipeline("grammar" + i);
            }

            System.out.println("\n Processing complete! All deliverables printed above and saved in the 'output' folder.");

        } catch (Exception e) {
            System.err.println("Fatal Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processGrammarPipeline(String grammarName) throws Exception {
        System.out.println("\n=======================================================");
        System.out.println(" Processing " + grammarName.toUpperCase());
        System.out.println("=======================================================\n");
        
        File grammarFile = new File("input/" + grammarName + ".txt");
        if (!grammarFile.exists()) {
            System.out.println("Skipping " + grammarName + " - File not found in 'input' folder.");
            return;
        }

        Grammar g = new Grammar();
        g.loadFromFile("input/" + grammarName + ".txt");
        GrammarTransformer.eliminateLeftRecursion(g);
        GrammarTransformer.applyLeftFactoring(g);
        
        String transformedGrammar = getGrammarString(g);
        System.out.println(transformedGrammar); 
        writeToFile("output/" + grammarName + "_transformed.txt", transformedGrammar);

        FirstFollow ff = new FirstFollow(g);
        ff.computeFirstSets();
        ff.computeFollowSets();
        
        String setsString = getSetsString(ff, g);
        System.out.println(setsString);
        writeToFile("output/" + grammarName + "_first_follow.txt", setsString);

        Parser parser = new Parser(g, ff);
        String tableString = parser.getTableString();
        System.out.println(tableString); 
        writeToFile("output/" + grammarName + "_parsing_table.txt", tableString);

        System.out.println("--- Parsing Valid Inputs for " + grammarName + " ---");
        processInputs("input/" + grammarName + "_valid.txt", "output/" + grammarName + "_valid_results.txt", parser);
        
        System.out.println("\n--- Parsing Error Inputs for " + grammarName + " ---");
        processInputs("input/" + grammarName + "_errors.txt", "output/" + grammarName + "_error_results.txt", parser);
    }

    private static void processInputs(String inputPath, String outputPath, Parser parser) throws Exception {
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.out.println("No input file found at: " + inputPath);
            return; 
        }

        List<String> lines = Files.readAllLines(Paths.get(inputPath));
        StringBuilder fileResults = new StringBuilder();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] tokens = line.trim().split("\\s+");
            
            ErrorHandler errorHandler = new ErrorHandler();
            Tree tree = parser.parse(tokens, errorHandler);
            
            StringBuilder singleResult = new StringBuilder();
            singleResult.append(parser.getLastTrace()).append("\n");
            
            if (tree != null && !errorHandler.hasErrors()) {
                singleResult.append("=== PARSE TREE ===\n");
                singleResult.append(tree.getTreeString("", true)).append("\n");
            }
            
            if (errorHandler.hasErrors()) {
                singleResult.append(errorHandler.getErrorReport()).append("\n");
            }
            singleResult.append("====================================================\n");
            
            String resultStr = singleResult.toString();
            System.out.println(resultStr); // PRINT TO CONSOLE
            fileResults.append(resultStr).append("\n"); // APPEND FOR FILE
        }
        
        writeToFile(outputPath, fileResults.toString());
    }

    private static void writeToFile(String filename, String content) throws Exception {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
        }
    }

    private static String getGrammarString(Grammar g) {
        StringBuilder sb = new StringBuilder("--- Transformed Context Free Grammar ---\n");
        for (String nt : g.rules.keySet()) {
            sb.append(nt).append(" -> ");
            List<List<String>> prods = g.rules.get(nt);
            for (int i = 0; i < prods.size(); i++) {
                sb.append(String.join(" ", prods.get(i)));
                if (i < prods.size() - 1) sb.append(" | ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String getSetsString(FirstFollow ff, Grammar g) {
        StringBuilder sb = new StringBuilder("--- FIRST Sets ---\n");
        for (String nt : g.nonTerminals) sb.append("FIRST(").append(nt).append(") = ").append(ff.firstSets.get(nt)).append("\n");
        sb.append("\n--- FOLLOW Sets ---\n");
        for (String nt : g.nonTerminals) sb.append("FOLLOW(").append(nt).append(") = ").append(ff.followSets.get(nt)).append("\n");
        return sb.toString();
    }
}