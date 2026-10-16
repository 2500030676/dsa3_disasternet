package edu.disasternet.utils;

import java.util.Scanner;

/** Shared retrying readers for numeric terminal input. */
public final class ConsoleInput {
    private ConsoleInput() {}

    public static int readInt(Scanner scanner, String prompt, int minimum) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(value);
                if (number >= minimum) return number;
            } catch (NumberFormatException ignored) { }
            System.out.println(ConsoleColors.color("[WARNING] Invalid input. Please try again.", ConsoleColors.YELLOW));
        }
    }
}
