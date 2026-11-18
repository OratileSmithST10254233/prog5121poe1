import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Message {
    private static final int MAX_LENGTH = 250;
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<String> messageIDs = new ArrayList<>();
    private static final List<String> messageHashes = new ArrayList<>();
    private static int totalMessagesSent = 0;
    private static String storagePath = "storedMessages.json";

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;
    private String sender = "";
    private boolean tracked = false;

    public Message(int messageNumber, String recipient, String messageText) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText == null ? "" : messageText;
        this.messageHash = createMessageHash();
    }

    public static String generateMessageID() {
        long id = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return String.format("%010d", id);
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.matches("[0-9]{1,10}");
    }

    public String messageIDGenerated() {
        return "Message ID generated: " + messageID;
    }

    public static String validateRecipient(String recipient) {
        if (new Login().checkCellPhoneNumber(recipient)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    public String checkRecipientCell() {
        return validateRecipient(recipient);
    }

    public static String validateLength(String text) {
        int length = text == null ? 0 : text.length();
        if (length <= MAX_LENGTH) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by " + (length - MAX_LENGTH) + "; please reduce the size.";
    }

    public String checkMessageLength() {
        return validateLength(messageText);
    }

    public String createMessageHash() {
        String idPart = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        String[] words = messageText.trim().split("\\s+");
        String firstWord = cleanWord(words[0]);
        String lastWord = cleanWord(words[words.length - 1]);
        return (idPart + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    private static String cleanWord(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(this);
                totalMessagesSent++;
                track();
                return "Message successfully sent.";
            case 2:
                disregardedMessages.add(this);
                track();
                return "Press 0 to delete the message.";
            case 3:
                storeMessage();
                track();
                return "Message successfully stored.";
            default:
                return "Invalid option; please choose 1 to send, 2 to disregard or 3 to store the message.";
        }
    }

    public String deleteMessage() {
        sentMessages.remove(this);
        storedMessages.remove(this);
        disregardedMessages.remove(this);
        untrack();
        return "Message deleted.";
    }

    public String getMessageDetails() {
        String nl = System.lineSeparator();
        return "Message ID: " + messageID + nl + "Message Hash: " + messageHash + nl + "Recipient: " + recipient + nl + "Message: " + messageText;
    }

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

    public void storeMessage() {
        if (!storedMessages.contains(this)) {
            storedMessages.add(this);
        }
        saveStoredMessages();
    }

    public static void saveStoredMessages() {
        try {
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < storedMessages.size(); i++) {
                Message m = storedMessages.get(i);
                String escapedText = m.messageText.replace("\\", "\\\\").replace("\"", "\\\"");
                sb.append("  {\n")
                  .append("    \"messageID\": \"").append(m.messageID).append("\",\n")
                  .append("    \"messageNumber\": ").append(m.messageNumber).append(",\n")
                  .append("    \"messageHash\": \"").append(m.messageHash).append("\",\n")
                  .append("    \"recipient\": \"").append(m.recipient).append("\",\n")
                  .append("    \"message\": \"").append(escapedText).append("\",\n")
                  .append("    \"sender\": \"").append(m.sender).append("\"\n")
                  .append("  }");
                if (i < storedMessages.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            Files.writeString(Path.of(storagePath), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not save to JSON: " + e.getMessage());
        }
    }

    public static void setStoragePath(String path) {
        storagePath = path;
    }

    public static List<Message> getStoredMessages() {
        return Collections.unmodifiableList(storedMessages);
    }

    public static void clearAll() {
        sentMessages.clear();
        storedMessages.clear();
        disregardedMessages.clear();
        messageIDs.clear();
        messageHashes.clear();
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

    private void track() {
        if (!tracked) {
            if (!messageIDs.contains(messageID)) messageIDs.add(messageID);
            if (!messageHashes.contains(messageHash)) messageHashes.add(messageHash);
            tracked = true;
        }
    }

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
            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (content.isBlank() || !content.trim().startsWith("[")) {
                return 0;
            }
            String[] blocks = content.split("\\},\\s*\\{");
            for (String block : blocks) {
                String id = extractJsonField(block, "messageID");
                int number = 0;
                try {
                    number = Integer.parseInt(extractJsonField(block, "messageNumber"));
                } catch (Exception e) {}
                String recipient = extractJsonField(block, "recipient");
                String text = extractJsonField(block, "message");
                String sender = extractJsonField(block, "sender");

                if (!id.isEmpty() && !text.isEmpty()) {
                    Message m = new Message(id, number, recipient, text);
                    m.setSender(sender);
                    m.track();
                    storedMessages.add(m);
                }
            }
        } catch (Exception e) {}
        return storedMessages.size();
    }

    private static String extractJsonField(String json, String field) {
        try {
            String search = "\"" + field + "\":";
            int idx = json.indexOf(search);
            if (idx == -1) return "";
            int start = idx + search.length();
            while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n' || json.charAt(start) == '\r' || json.charAt(start) == '\t')) {
                start++;
            }
            if (start >= json.length()) return "";
            if (json.charAt(start) == '"') {
                start++;
                int end = start;
                while (end < json.length()) {
                    if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') {
                        break;
                    }
                    end++;
                }
                if (end < json.length()) {
                    return json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
                }
                return "";
            } else {
                int end = start;
                while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '\n' && json.charAt(end) != '}') {
                    end++;
                }
                return json.substring(start, end).trim();
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static String displaySenderAndRecipientOfStored() {
        if (storedMessages.isEmpty()) return "There are no stored messages.";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < storedMessages.size(); i++) {
            Message message = storedMessages.get(i);
            if (i > 0) sb.append(System.lineSeparator());
            sb.append("Sender: ").append(message.sender.isBlank() ? "Ora Smith" : message.sender)
                    .append(" | Recipient: ").append(message.recipient);
        }
        return sb.toString();
    }

    public static String longestStoredMessage() {
        String longest = null;
        for (Message message : storedMessages) {
            if (longest == null || message.messageText.length() > longest.length()) {
                longest = message.messageText;
            }
        }
        return longest == null ? "There are no stored messages." : longest;
    }

    public static Message findMessageByID(String id) {
        String wanted = id == null ? "" : id.trim();
        List<List<Message>> arrays = List.of(sentMessages, storedMessages, disregardedMessages);
        for (List<Message> array : arrays) {
            for (Message message : array) {
                if (message.messageID.equals(wanted)) return message;
            }
        }
        return null;
    }

    public static String searchByMessageID(String id) {
        Message message = findMessageByID(id);
        if (message == null) return null;
        return "Recipient: " + message.recipient + System.lineSeparator() + "Message: " + message.messageText;
    }

    public static List<Message> findMessagesByRecipient(String recipient) {
        String wanted = recipient == null ? "" : recipient.trim();
        List<Message> found = new ArrayList<>();
        for (Message message : sentMessages) {
            if (message.recipient.equals(wanted)) found.add(message);
        }
        for (Message message : storedMessages) {
            if (message.recipient.equals(wanted)) found.add(message);
        }
        return found;
    }

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

    public static String deleteMessageByHash(String hash) {
        String wanted = hash == null ? "" : hash.trim();
        Message target = null;
        boolean wasStored = false;
        for (Message message : storedMessages) {
            if (message.messageHash.equalsIgnoreCase(wanted)) { target = message; wasStored = true; break; }
        }
        if (target == null) {
            for (Message message : sentMessages) {
                if (message.messageHash.equalsIgnoreCase(wanted)) { target = message; break; }
            }
        }
        if (target == null) return "No message was found with the message hash " + wanted + ".";
        if (wasStored) { storedMessages.remove(target); saveStoredMessages(); }
        else { sentMessages.remove(target); }
        target.untrack();
        return "Message: \"" + target.messageText + "\" successfully deleted.";
    }

    public static String sentMessagesReport() {
        String nl = System.lineSeparator();
        if (sentMessages.isEmpty()) return "SENT MESSAGES REPORT" + nl + "There are no sent messages.";
        StringBuilder sb = new StringBuilder("SENT MESSAGES REPORT");
        for (Message message : sentMessages) {
            sb.append(nl).append(nl).append("Message Hash: ").append(message.messageHash).append(nl)
                    .append("Recipient: ").append(message.recipient).append(nl).append("Message: ").append(message.messageText);
        }
        return sb.toString();
    }

    public static String storedMessagesReport() {
        String nl = System.lineSeparator();
        if (storedMessages.isEmpty()) return "STORED MESSAGES REPORT" + nl + "There are no stored messages.";
        StringBuilder sb = new StringBuilder("STORED MESSAGES REPORT");
        for (Message message : storedMessages) {
            sb.append(nl).append(nl).append("Message ID: ").append(message.messageID).append(nl)
                    .append("Message Hash: ").append(message.messageHash).append(nl)
                    .append("Sender: ").append(message.sender.isBlank() ? "Ora Smith" : message.sender).append(nl)
                    .append("Recipient: ").append(message.recipient).append(nl).append("Message: ").append(message.messageText);
        }
        return sb.toString();
    }
}
