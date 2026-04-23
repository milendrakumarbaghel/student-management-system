package input;

import java.util.Scanner;

public class ConsoleInput {

    private final Scanner scanner;

    public ConsoleInput() {
        this.scanner = new Scanner(System.in);
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
            System.out.println("Value cannot be empty.");
        }
    }
}

