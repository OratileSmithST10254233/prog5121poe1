import java.util.Scanner;

/**
 * Console-only entry point for Part 1 of the PROG5121 POE
 * (Registration and login feature). No GUI / JOptionPane is used,
 * per the brief's requirements.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login login = new Login();

        System.out.println("=== User Registration ===");

        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine();

        System.out.print("Enter username (must contain '_' and be 5 characters or fewer): ");
        String username = scanner.nextLine();

        System.out.print("Enter password (8+ chars, capital letter, number, special character): ");
        String password = scanner.nextLine();

        System.out.print("Enter South African cell number (e.g. +27831234567): ");
        String cellPhoneNumber = scanner.nextLine();

        String registrationMessage = login.registerUser(firstName, lastName, username, password, cellPhoneNumber);
        System.out.println(registrationMessage);

        if (!login.isRegistered()) {
            scanner.close();
            return;
        }

        System.out.println();
        System.out.println("=== User Login ===");

        System.out.print("Enter username: ");
        String loginUsername = scanner.nextLine();

        System.out.print(
}