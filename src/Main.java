import java.util.EnumSet;
import java.util.ArrayList;
import java.util.Scanner;

import java.lang.Character;
import java.lang.Math;
import java.lang.StringBuilder;

public class Main {
    public static void main(String[] args) {
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

            var result = root.evaluate();
            System.out.println("-> " + result.toString());
        }
    }
}