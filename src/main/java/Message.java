import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public void setSender(String sender) {
        this.sender = sender == null ? "" : sender;
    }

    // ---------------------------------------------------------------
    // Part 3: the arrays (populated while the program runs)
    // ---------------------------------------------------------------

    /** Adds this message's ID and hash to the ID and hash arrays (once). */
    private void track() {
        if (!tracked) {
            messageIDs.add(messageID);
            messageHashes.add(messageHash);
            tracked = true;
        }
    }

    /** Removes this message's ID and hash from the ID and hash arrays. */
    private void untrack() {
        if (tracked) {
            messageIDs.remove(messageID);
            messageHashes.remove(messageHash);
            tracked = false;
        }
    }

    public static List<String> getSentMessageTexts() {
        return textsOf(sentMessages);
    }

    public static List<String> getDisregardedMessageTexts() {
        return textsOf(disregardedMessages);
    }

    public static List<String> getStoredMessageTexts() {
        return textsOf(storedMessages);
    }

    public static List<String> getMessageHashes() {
        return Collections.unmodifiableList(new ArrayList<>(messageHashes));
    }

    public static List<String> getMessageIDs() {
        return Collections.unmodifiableList(new ArrayList<>(messageIDs));
    }

    private static List<String> textsOf(List<Message> messages) {
        List<String> texts = new ArrayList<>();
        for (Message message : messages) {
            texts.add(message.messageText);
        }
        return texts;
    }

    // ---------------------------------------------------------------
    // Part 3: reading the JSON file back into the stored messages array
    // ---------------------------------------------------------------

    /**
     * Reads the JSON file written by storeMessage() into the stored messages
     * array. Returns how many messages were loaded (0 if there is no file yet).
     */
    public static int loadStoredMessages() {
        for (Message old : storedMessages) {
            old.untrack();
        }
        storedMessages.clear();

        Path path = Path.of(storagePath);
        if (!Files.exists(path)) {
            return 0;
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            for (Map<String, String> fields : new JsonReader(json).readArrayOfObjects()) {
                int number = 0;
                try {
                    number = Integer.parseInt(fields.getOrDefault("messageNumber", "0"));
                } catch (NumberFormatException e) {
                    // keep 0 if the number is damaged
                }
                Message message = new Message(fields.getOrDefault("messageID", ""), number,
                        fields.getOrDefault("recipient", ""), fields.getOrDefault("message", ""));
                message.setSender(fields.getOrDefault("sender", ""));
                message.track();
                storedMessages.add(message);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Could not read the stored messages from the JSON file: " + e.getMessage());
            for (Message partial : storedMessages) {
                partial.untrack();
            }
            storedMessages.clear();
        }
        return storedMessages.size();
    }

    /**
     * A small reader for the JSON this class writes: an array of objects
     * whose values are strings or numbers. (Written by hand so no extra
     * library is needed; see json.org for the JSON format.)
     */
    private static class JsonReader {
        private final String text;
        private int pos = 0;

        JsonReader(String text) {
            this.text = text;
        }

        List<Map<String, String>> readArrayOfObjects() {
            List<Map<String, String>> objects = new ArrayList<>();
            skipWhitespace();
            expect('[');
            skipWhitespace();
            if (peek() == ']') {
                pos++;
                return objects;
            }
            while (true) {
                objects.add(readObject());
                skipWhitespace();
                char c = next();
                if (c == ']') {
                    return objects;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("expected ',' or ']' at position " + (pos - 1));
                }
            }
        }

        private Map<String, String> readObject() {
            Map<String, String> fields = new LinkedHashMap<>();
            skipWhitespace();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                pos++;
                return fields;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                String value = peek() == '"' ? readString() : readNumberToken();
                fields.put(key, value);
                skipWhitespace();
                char c = next();
                if (c == '}') {
                    return fields;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("expected ',' or '}' at position " + (pos - 1));
                }
            }
        }

        private String readString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                char escaped = next();
                switch (escaped) {
                    case '"':  sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'u':
                        if (pos + 4 > text.length()) {
                            throw new IllegalArgumentException("bad \\u escape");
                        }
                        sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        pos += 4;
                        break;
                    default:
                        throw new IllegalArgumentException("bad escape \\" + escaped);
                }
            }
        }

        private String readNumberToken() {
            int start = pos;
            while (pos < text.length() && ",}] \n\r\t".indexOf(text.charAt(pos)) < 0) {
                pos++;
            }
            if (start == pos) {
                throw new IllegalArgumentException("expected a value at position " + pos);
            }
            return text.substring(start, pos);
        }

        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        private char peek() {
            if (pos >= text.length()) {
                throw new IllegalArgumentException("unexpected end of file");
            }
            return text.charAt(pos);
        }

        private char next() {
            char c = peek();
            pos++;
            return c;
        }

        private void expect(char expected) {
            char c = next();
            if (c != expected) {
                throw new IllegalArgumentException("expected '" + expected + "' at position " + (pos - 1));
            }
        }
    }

    // ---------------------------------------------------------------
    // Part 3: using the arrays (the "Stored Messages" menu)
    // ---------------------------------------------------------------

    /** (a) The sender and recipient of every stored message. */
    public static String displaySenderAndRecipientOfStored() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message message : storedMessages) {
            if (sb.length() > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append("Sender: ").append(message.sender.isBlank() ? "Unknown sender" : message.sender)
                    .append(" | Recipient: ").append(message.recipient);
        }
        return sb.toString();
    }

    /** (b) The longest stored message (the first one if there is a tie). */
    public static String longestStoredMessage() {
        String longest = null;
        for (Message message : storedMessages) {
            if (longest == null || message.messageText.length() > longest.length()) {
                longest = message.messageText;
            }
        }
        return longest == null ? "There are no stored messages." : longest;
    }

    /** Finds a message by its ID in the sent, stored and disregarded arrays. */
    public static Message findMessageByID(String id) {
        String wanted = id == null ? "" : id.trim();
        List<List<Message>> arrays = List.of(sentMessages, storedMessages, disregardedMessages);
        for (List<Message> array : arrays) {
            for (Message message : array) {
                if (message.messageID.equals(wanted)) {
                    return message;
                }
            }
        }
        return null;
    }

    /** (c) Search for a message ID and display the recipient and message. */
    public static String searchByMessageID(String id) {
        Message message = findMessageByID(id);
        if (message == null) {
            return "No message was found with the message ID " + (id == null ? "" : id.trim()) + ".";
        }
        return "Recipient: " + message.recipient + System.lineSeparator()
                + "Message: " + message.messageText;
    }

    /** All the sent or stored messages for one recipient. */
    public static List<Message> findMessagesByRecipient(String recipient) {
        String wanted = recipient == null ? "" : recipient.trim();
        List<Message> found = new ArrayList<>();
        for (Message message : sentMessages) {
            if (message.recipient.equals(wanted)) {
                found.add(message);
            }
        }
        for (Message message : storedMessages) {
            if (message.recipient.equals(wanted)) {
                found.add(message);
            }
        }
        return found;
    }

    /** (d) Search for all the messages sent or stored for a recipient. */
    public static String searchByRecipient(String recipient) {
        List<Message> found = findMessagesByRecipient(recipient);
        if (found.isEmpty()) {
            return "No messages were found for the recipient " + (recipient == null ? "" : recipient.trim()) + ".";
        }
        StringBuilder sb = new StringBuilder();
        for (Message message : found) {
            if (sb.length() > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append(message.messageText);
        }
        return sb.toString();
    }

    /** (e) Delete a stored (or sent) message using its message hash. */
    public static String deleteMessageByHash(String hash) {
        String wanted = hash == null ? "" : hash.trim();
        Message target = null;
        boolean wasStored = false;
        for (Message message : storedMessages) {
            if (message.messageHash.equalsIgnoreCase(wanted)) {
                target = message;
                wasStored = true;
                break;
            }
        }
        if (target == null) {
            for (Message message : sentMessages) {
                if (message.messageHash.equalsIgnoreCase(wanted)) {
                    target = message;
                    break;
                }
            }
        }
        if (target == null) {
            return "No message was found with the message hash " + wanted + ".";
        }
        if (wasStored) {
            storedMessages.remove(target);
            saveStoredMessages();
        } else {
            sentMessages.remove(target);
        }
        target.untrack();
        return "Message: \"" + target.messageText + "\" successfully deleted.";
    }

    /** Report of all the sent messages: hash, recipient and message. */
    public static String sentMessagesReport() {
        String nl = System.lineSeparator();
        if (sentMessages.isEmpty()) {
            return "SENT MESSAGES REPORT" + nl + "There are no sent messages.";
        }
        StringBuilder sb = new StringBuilder("SENT MESSAGES REPORT");
        for (Message message : sentMessages) {
            sb.append(nl).append(nl)
                    .append("Message Hash: ").append(message.messageHash).append(nl)
                    .append("Recipient: ").append(message.recipient).append(nl)
                    .append("Message: ").append(message.messageText);
        }
        return sb.toString();
    }

    /** (f) Report with the full details of all the stored messages. */
    public static String storedMessagesReport() {
        String nl = System.lineSeparator();
        if (storedMessages.isEmpty()) {
            return "STORED MESSAGES REPORT" + nl + "There are no stored messages.";
        }
        StringBuilder sb = new StringBuilder("STORED MESSAGES REPORT");
        for (Message message : storedMessages) {
            sb.append(nl).append(nl)
                    .append("Message ID: ").append(message.messageID).append(nl)
                    .append("Message Hash: ").append(message.messageHash).append(nl)
                    .append("Sender: ").append(message.sender.isBlank() ? "Unknown sender" : message.sender).append(nl)
                    .append("Recipient: ").append(message.recipient).append(nl)
                    .append("Message: ").append(message.messageText);
        }
        return sb.toString();
    }
}


