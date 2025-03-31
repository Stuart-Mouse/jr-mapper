import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import java.lang.StringBuilder;

public class Main {
    public static void main(String[] args) {

        try {
            Path filePath = Paths.get("test.rmap");
            String input = Files.readString(filePath);

            var parser = new Parser();
            var root = parser.parseFile(input);
            if (root == null) {
                throw new RuntimeException("Failed to parse file!");
            }

            parser.metaData = new Parser.MetaData();
            parser.setVariable("meta", parser.metaData, Parser.MetaData.class);

            var meta2 = new Parser.MetaData();
            parser.setVariable("meta2", meta2, Parser.MetaData.class);

            if (!parser.typecheck()) {
                throw new RuntimeException("Error: failed to typecheck file.");
            }
            if (!parser.evaluate()) {
                throw new RuntimeException("Error: failed to evaluate file.");
            }

            System.out.println(root.toString());

            System.out.println("parser.metaData.name: " + parser.metaData.name);
            System.out.println("parser.metaData.id: " + parser.metaData.id);

        } catch(Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        }

        // REPL Test

        System.out.println("Input numeric expression to evaluate or type \"exit\" to quit.");

        var scanner = new Scanner(System.in);
        var parser  = new Parser();
        while (true) {
            System.out.print("> ");
            var input = scanner.nextLine();
            if (input == null || input.isEmpty()) continue;
            if (input.startsWith("exit")) break;

            // NOTE: as a hack, we are checking the first character of input, and if it is a '!' then we will treat the input as an expression, else it is a declaration
            Node root;
            if (input.charAt(0) == '!') {
                root = parser.parseExpression(input.substring(1));
                if (root == null) {
                    System.out.println("Failed to parse expression!");
                    continue;
                }
                try { root.typecheck(null); }
                catch (Exception e) {
                    System.out.println("Failed to typecheck expression!");
                    continue;
                }
            } else {
                root = parser.parseDeclaration(input);
                if (root == null) {
                    System.out.println("Failed to parse declaration!");
                    continue;
                }
                try { root.typecheck(null); }
                catch (Exception e) {
                    System.out.println("Failed to typecheck expression!");
                    continue;
                }
            }

            var result = root.evaluate(null);
            System.out.println("-> " + result.toString());
        }
    }
}