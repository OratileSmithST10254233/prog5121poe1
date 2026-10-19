import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Message class for the PROG5121 POE chat app (QuickChat) - Part 2.
 *
 * Handles the message ID, recipient and length checks, the message hash,
 * sending / storing / disregarding a message, and storing messages in JSON.
 */
public class Message {

    private static final int MAX_LENGTH = 250;

    // These lists live for as long as the program is running
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static int totalMessagesSent = 0;
    private static String storagePath = "storedMessages.json";

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    /** Creates a message with a randomly generated ten-digit message ID. */
    public Message(int messageNumber, String recipient, String messageText) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    /** Creates a message with a given ID (used by the unit tests). */
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText == null ? "" : messageText;
        this.messageHash = createMessageHash();
    }

    // ---------------------------------------------------------------
    // Message ID
    // ---------------------------------------------------------------

    /** Randomly generates a ten-digit number (leading zeros allowed). */
    public static String generateMessageID() {
        long id = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return String.format("%010d", id);
    }

    /** The message ID must be digits only and no more than ten characters. */
    public boolean checkMessageID() {
        return messageID != null && messageID.matches("[0-9]{1,10}");
    }

    public String messageIDGenerated() {
        return "Message ID generated: " + messageID;
    }

    // ---------------------------------------------------------------
    // Recipient and message length checks
    // ---------------------------------------------------------------

    /** Re-uses the cell phone check from Part 1 (the Login class). */
    public static String validateRecipient(String recipient) {
        if (new Login().checkCellPhoneNumber(recipient)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.";
    }

    public String checkRecipientCell() {
        return validateRecipient(recipient);
    }

    public static String validateLength(String text) {
        int length = text == null ? 0 : text.length();
        if (length <= MAX_LENGTH) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by " + (length - MAX_LENGTH)
                + "; please reduce the size.";
    }

    public String checkMessageLength() {
        return validateLength(messageText);
    }

    // ---------------------------------------------------------------
    // Message hash
    // ---------------------------------------------------------------

    /**
     * First two digits of the message ID, a colon, the message number,
     * a colon, then the first and last words of the message - all in caps.
     * Example: 00:0:HITONIGHT
     */
    public String createMessageHash() {
        String idPart = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        String[] words = messageText.trim().split("\\s+");
        String firstWord = cleanWord(words[0]);
        String lastWord = cleanWord(words[words.length - 1]);
        return (idPart + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    // Removes punctuation such as the "?" in "tonight?"
    private static String cleanWord(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    // ---------------------------------------------------------------
    // Send / disregard / store
    // ---------------------------------------------------------------

    /**
     * 1 = send, 2 = disregard, 3 = store to send later.
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(this);
                totalMessagesSent++;
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option; please choose 1 to send, 2 to disregard or 3 to store the message.";
        }
    }

    /** Deletes the message (used after the user presses 0). */
    public String deleteMessage() {
        sentMessages.remove(this);
        storedMessages.remove(this);
        return "Message deleted.";
    }

    // ---------------------------------------------------------------
    // Displaying messages
    // ---------------------------------------------------------------

    /** Message ID, Message Hash, Recipient, Message - in that order. */
    public String getMessageDetails() {
        String nl = System.lineSeparator();
        return "Message ID: " + messageID + nl
                + "Message Hash: " + messageHash + nl
                + "Recipient: " + recipient + nl
                + "Message: " + messageText;
    }

    /** Returns all the messages sent while the program is running. */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message message : sentMessages) {
            sb.append(message.getMessageDetails());
            sb.append(System.lineSeparator()).append(System.lineSeparator());
        }
        return sb.toString().trim();
    }

    public static int returnTotalMessages() {
        return totalMessagesSent;
    }

    // ---------------------------------------------------------------
    // JSON storage
    // ---------------------------------------------------------------

    /** Adds the message to the stored list and saves the list to a JSON file. */
    public void storeMessage() {
        storedMessages.add(this);
        try {
            Files.writeString(Path.of(storagePath), storedMessagesToJson(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not save the message to the JSON file: " + e.getMessage());
        }
    }

    public static String storedMessagesToJson() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            json.append(storedMessages.get(i).toJsonObject());
            if (i < storedMessages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]\n");
        return json.toString();
    }

    private String toJsonObject() {
        return "  {\n"
                + "    \"messageID\": \"" + escapeJson(messageID) + "\",\n"
                + "    \"messageNumber\": " + messageNumber + ",\n"
                + "    \"messageHash\": \"" + escapeJson(messageHash) + "\",\n"
                + "    \"recipient\": \"" + escapeJson(recipient) + "\",\n"
                + "    \"message\": \"" + escapeJson(messageText) + "\"\n"
                + "  }";
    }

    // Escapes characters that are not allowed raw inside a JSON string
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // Helpers for the app and the unit tests
    // ---------------------------------------------------------------

    public static void setStoragePath(String path) {
        storagePath = path;
    }

    public static List<Message> getStoredMessages() {
        return Collections.unmodifiableList(storedMessages);
    }

    /** Clears everything held in memory (used between unit tests). */
    public static void clearAll() {
        sentMessages.clear();
        storedMessages.clear();
        totalMessagesSent = 0;
    }

    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }
}
