package src;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Ensure output directory exists
            new File("output").mkdirs();

            // Process Grammar 1 (Standard)
            processGrammarPipeline("grammar1");
            
            // Process Grammar 2 (Left Recursive)
            processGrammarPipeline("grammar2");

            System.out.println("\n✅ Processing complete! All deliverables printed above and saved in the 'output' folder.");

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

        // 1. Load and Transform
        Grammar g = new Grammar();
        g.loadFromFile("input/" + grammarName + ".txt");
        GrammarTransformer.eliminateLeftRecursion(g);
        GrammarTransformer.applyLeftFactoring(g);
        
        String transformedGrammar = getGrammarString(g);
        System.out.println(transformedGrammar); // PRINT TO CONSOLE
        writeToFile("output/" + grammarName + "_transformed.txt", transformedGrammar);

        // 2. Compute Sets
        FirstFollow ff = new FirstFollow(g);
        ff.computeFirstSets();
        ff.computeFollowSets();
        
        String setsString = getSetsString(ff, g);
        System.out.println(setsString); // PRINT TO CONSOLE
        writeToFile("output/" + grammarName + "_first_follow.txt", setsString);

        // 3. Build Parser Table
        Parser parser = new Parser(g, ff);
        String tableString = parser.getTableString();
        System.out.println(tableString); // PRINT TO CONSOLE
        writeToFile("output/" + grammarName + "_parsing_table.txt", tableString);

        // 4. Process Separated Input Files
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
            
            // Build the output block for this specific input string
            StringBuilder singleResult = new StringBuilder();
            singleResult.append(parser.getLastTrace()).append("\n");
            
            // Append Parse Tree if successful
            if (tree != null && !errorHandler.hasErrors()) {
                singleResult.append("=== PARSE TREE ===\n");
                singleResult.append(tree.getTreeString("", true)).append("\n");
            }
            
            // Append Errors if Panic Mode triggered
            if (errorHandler.hasErrors()) {
                singleResult.append(errorHandler.getErrorReport()).append("\n");
            }
            singleResult.append("====================================================\n");
            
            String resultStr = singleResult.toString();
            System.out.println(resultStr); // PRINT TO CONSOLE
            fileResults.append(resultStr).append("\n"); // APPEND FOR FILE
        }
        
        // Write the combined results for all strings in the file to the output folder
        writeToFile(outputPath, fileResults.toString());
    }

    private static void writeToFile(String filename, String content) throws Exception {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
        }
    }

    // Helper methods to convert print statements to Strings for file writing
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