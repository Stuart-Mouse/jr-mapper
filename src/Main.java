import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import java.lang.StringBuilder;

public class Main {
    public static void main(String[] args) {

        // TODO: test objects and arrays
        //       cannot yet typecheck, so just parse and serialize
        try {
            Path filePath = Paths.get("test.rmap");
            String input = Files.readString(filePath);

            var parser = new Parser();
            var root = parser.parseFile(input);
            if (root == null) {
                throw new RuntimeException("Failed to parse file!");
            }
//            var meta = parser.getMetaNode();
//            if (!meta.typecheck(Parser.MetaData.class)) {
//                throw new RuntimeException("Failed to typecheck file metadata!");
//            }
            parser.metaData = new Parser.MetaData();
            parser.setVariable("meta", parser.metaData, Parser.MetaData.class);

            var meta2 = new Parser.MetaData();
            parser.setVariable("meta2", meta2, Parser.MetaData.class);
//            parser.metaData = (Parser.MetaData)(meta.evaluate(parser.metaData));
//            if (parser.metaData == null) {
//                throw new RuntimeException("Failed to evaluate file metadata!");
//            }
            if (!parser.typecheck()) {
                throw new RuntimeException("Failed to typecheck file!");
            }
            if (!parser.evaluate()) {
                throw new RuntimeException("Failed to evaluate file!");
            }

            var sb = new StringBuilder();
            root.serialize(sb);
            System.out.println(sb.toString());

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
                if (!root.typecheck(null)) {
                    System.out.println("Failed to typecheck expression!");
                    continue;
                }
            } else {
                root = parser.parseDeclaration(input);
                if (root == null) {
                    System.out.println("Failed to parse declaration!");
                    continue;
                }
                if (!root.typecheck(null)) {
                    System.out.println("Failed to typecheck declaration!");
                    continue;
                }
            }

            var result = root.evaluate(null);
            System.out.println("-> " + result.toString());
        }
    }
}