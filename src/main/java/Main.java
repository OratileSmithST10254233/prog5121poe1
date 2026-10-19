import java.util.Scanner;

/**
 * Console-only entry point for Part 2 of the PROG5121 POE (QuickChat).
 * No GUI / JOptionPane is used, per the brief.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Login login = new Login();

        // Part 1: users must register and log in before they can send messages
        registerAndLogin(login);

        System.out.println();
        System.out.println("Welcome to QuickChat.");

        int maxMessages = readPositiveInt("How many messages would you like to enter? ");
        int messagesEntered = 0;
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            int option = readInt("Choose an option: ");

            switch (option) {
                case 1:
                    if (messagesEntered >= maxMessages) {
                        System.out.println("You have already entered the " + maxMessages
                                + " message(s) you set at the start.");
                    } else {
                        while (messagesEntered < maxMessages) {
                            enterMessage(messagesEntered);
                            messagesEntered++;
                        }
                        System.out.println();
                        System.out.println("Total messages sent: " + Message.returnTotalMessages());
                    }
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option, please choose 1, 2 or 3.");
            }
        }

        scanner.close();
    }

    // ---------------------------------------------------------------
    // Registration and login (Part 1)
    // ---------------------------------------------------------------

    private static void registerAndLogin(Login login) {
        System.out.println("=== User Registration ===");
        while (!login.isRegistered()) {
            System.out.print("Enter first name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter last name: ");
            String lastName = scanner.nextLine();

            System.out.print("Enter username (must contain '_' and be 5 characters or fewer): ");
            String username = scanner.nextLine();

            System.out.print("Enter password (8+ chars, capital letter, number, special character): ");
            String password = scanner.nextLine();

            System.out.print("Enter cell number with country code (e.g. +27831234567): ");
            String cellPhoneNumber = scanner.nextLine();

            System.out.println(login.registerUser(firstName, lastName, username, password, cellPhoneNumber));
        }

        System.out.println();
        System.out.println("=== User Login ===");
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Enter username: ");
            String loginUsername = scanner.nextLine();

            System.out.print("Enter password: ");
            String loginPassword = scanner.nextLine();

            System.out.println(login.returnLoginStatus(loginUsername, loginPassword));
            loggedIn = login.loginUser(loginUsername, loginPassword);
        }
    }

    // ---------------------------------------------------------------
    // Entering one message (Part 2)
    // ---------------------------------------------------------------

    private static void enterMessage(int index) {
        System.out.println();
        System.out.println("--- Message " + (index + 1) + " ---");

        // Recipient: an invalid number is reported, and the message can then
        // only be disregarded (it cannot be sent or stored)
        System.out.print("Enter the recipient's cell number (e.g. +27718693002): ");
        String recipient = scanner.nextLine().trim();
        String recipientResult = Message.validateRecipient(recipient);
        System.out.println(recipientResult);
        boolean recipientValid = recipientResult.equals("Cell phone number successfully captured.");

        // Message text: keep asking until it is not empty and not over 250 characters
        String text;
        while (true) {
            System.out.print("Enter your message (250 characters max): ");
            text = scanner.nextLine();
            if (text.isBlank()) {
                System.out.println("The message cannot be empty.");
                continue;
            }
            String lengthResult = Message.validateLength(text);
            if (text.length() <= 250) {
                System.out.println(lengthResult);
                break;
            }
            System.out.println("Please enter a message of less than 250 characters.");
            System.out.println(lengthResult);
        }

        Message message = new Message(index, recipient, text);
        System.out.println(message.messageIDGenerated());

        System.out.println();
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        int choice;
        while (true) {
            choice = readInt("Choose an option (1-3): ");
            if (choice < 1 || choice > 3) {
                continue;
            }
            if (!recipientValid && choice != 2) {
                System.out.println("The recipient number is invalid, so this message cannot be sent or stored. "
                        + "Please disregard it (option 2).");
                continue;
            }
            break;
        }

        System.out.println(message.sentMessage(choice));

        if (choice == 1) {
            System.out.println();
            System.out.println(message.getMessageDetails());
        } else if (choice == 2) {
            int pressed;
            do {
                pressed = readInt("> ");
            } while (pressed != 0);
            System.out.println(message.deleteMessage());
        }
    }

    // ---------------------------------------------------------------
    // Input helpers
    // ---------------------------------------------------------------

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static int readPositiveInt(String prompt) {
        int value;
        do {
            value = readInt(prompt);
            if (value <= 0) {
                System.out.println("Please enter a number greater than 0.");
            }
        } while (value <= 0);
        return value;
    }
} 

