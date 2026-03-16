package src;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Load the Grammar
            Grammar grammar = new Grammar();
            grammar.loadFromFile("grammar.txt");
            grammar.printGrammar();

            // 2. Compute FIRST and FOLLOW sets
            FirstFollow ff = new FirstFollow(grammar);
            ff.computeFirstSets();
            ff.computeFollowSets();
            ff.printSets();

            // 3. Construct Parsing Table & Parse
            LL1Parser parser = new LL1Parser(grammar, ff);
            parser.printTable();

            // 4. Test parsing with a valid string: id + id * id
            String[] validInput = {"id", "+", "id", "*", "id"};
            parser.parse(validInput);

            // 5. Test parsing with an invalid string (to trigger Error Recovery)
            String[] invalidInput = {"id", "+", "*", "id"};
            parser.parse(invalidInput);

        } catch (IOException e) {
            System.err.println("Error reading grammar file: " + e.getMessage());
        }
    }
}