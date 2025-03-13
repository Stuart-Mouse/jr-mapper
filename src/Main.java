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

            var root = parser.parseExpression(input);
            if (!root.typecheck(null, null)) {
                System.out.println("Failed to typecheck expression!");
                continue;
            }

            var result = root.evaluate();
            System.out.println("-> " + result.toString());
        }
    }
}