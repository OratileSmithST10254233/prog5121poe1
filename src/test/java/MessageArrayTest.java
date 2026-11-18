import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Part 3 (arrays, searching, deleting and the report), using
 * the five test messages from the PROG5121POE brief. The test data is
 * entered by the developer here, which is what the brief means by
 * "Developer entry for test data".
 */
public class MessageArraysTest {

    @TempDir
    Path tempDir;

    // Test Data Messages 1 to 5 from the brief
    private static final String RECIPIENT_1 = "+27834557896";
    private static final String MESSAGE_1 = "Did you get the cake?";

    private static final String RECIPIENT_2 = "+27838884567";
    private static final String MESSAGE_2 = "Where are you? You are late! I have asked you to be on time.";

    private static final String RECIPIENT_3 = "+27834484567";
    private static final String MESSAGE_3 = "Yohoooo, I am at your gate.";

    // The brief labels message 4 "Developer 0838884567": it is used as the message ID
    // in the "search for messageID" test. The brief gives no recipient, so one is assumed.
    private static final String ID_4 = "0838884567";
    private static final String RECIPIENT_4 = "+27838884568";
    private static final String MESSAGE_4 = "It is dinner time!";

    private static final String RECIPIENT_5 = "+27838884567";
    private static final String MESSAGE_5 = "Ok, I am leaving without you.";

    // Fixed IDs so the hashes in the tests never change (real IDs are random)
    private static final String ID_1 = "0112345678";
    private static final String ID_2 = "0212345678";
    private static final String ID_3 = "0312345678";
    private static final String ID_5 = "0512345678";

    private Path jsonFile;

    @BeforeEach
    public void setUp() {
        Message.clearAll();
        jsonFile = tempDir.resolve("storedMessages.json");
        Message.setStoragePath(jsonFile.toString());
        populateTestData();
    }

    /** Puts each test message in the array that its flag says (Sent / Stored / Disregard). */
    private void populateTestData() {
        Message m1 = new Message(ID_1, 0, RECIPIENT_1, MESSAGE_1);
        m1.setSender("Ora Smith");
        m1.sentMessage(1); // Sent

        Message m2 = new Message(ID_2, 1, RECIPIENT_2, MESSAGE_2);
        m2.setSender("Ora Smith");
        m2.sentMessage(3); // Stored

        Message m3 = new Message(ID_3, 2, RECIPIENT_3, MESSAGE_3);
        m3.setSender("Ora Smith");
        m3.sentMessage(2); // Disregard

        Message m4 = new Message(ID_4, 3, RECIPIENT_4, MESSAGE_4);
        m4.setSender("Ora Smith");
        m4.sentMessage(1); // Sent

        Message m5 = new Message(ID_5, 4, RECIPIENT_5, MESSAGE_5);
        m5.setSender("Ora Smith");
        m5.sentMessage(3); // Stored
    }

    // --- Sent Messages array correctly populated ---

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList("Did you get the cake?", "It is dinner time!"),
                Message.getSentMessageTexts());
    }

    // --- The other arrays ---

    @Test
    public void testDisregardedMessagesArray() {
        assertEquals(Arrays.asList("Yohoooo, I am at your gate."),
                Message.getDisregardedMessageTexts());
    }

    @Test
    public void testStoredMessagesArray() {
        assertEquals(Arrays.asList(MESSAGE_2, MESSAGE_5), Message.getStoredMessageTexts());
    }

    @Test
    public void testMessageHashArray() {
        assertEquals(Arrays.asList("01:0:DIDCAKE", "02:1:WHERETIME", "03:2:YOHOOOOGATE",
                "08:3:ITTIME", "05:4:OKYOU"), Message.getMessageHashes());
    }

    @Test
    public void testMessageIDArray() {
        assertEquals(Arrays.asList(ID_1, ID_2, ID_3, ID_4, ID_5), Message.getMessageIDs());
    }

    // --- Display the longest message ---

    @Test
    public void testLongestStoredMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.longestStoredMessage());
    }

    // --- Search for messageID ---

    @Test
    public void testSearchForMessageID() {
        assertEquals("It is dinner time!", Message.findMessageByID("0838884567").getMessageText());
    }

    @Test
    public void testSearchForMessageIDDisplaysRecipientAndMessage() {
        String expected = "Recipient: " + RECIPIENT_4 + System.lineSeparator() + "Message: " + MESSAGE_4;
        assertEquals(expected, Message.searchByMessageID("0838884567"));
    }

    @Test
    public void testSearchForMessageIDNotFound() {
        assertNull(Message.findMessageByID("9999999999"));
    }

    // --- Search all the messages sent or stored for a particular recipient ---

    @Test
    public void testSearchByRecipient() {
        List<Message> found = Message.findMessagesByRecipient("+27838884567");
        assertEquals(2, found.size());
        assertEquals("Where are you? You are late! I have asked you to be on time.", found.get(0).getMessageText());
        assertEquals("Ok, I am leaving without you.", found.get(1).getMessageText());
    }

    // --- Delete a message using a message hash ---

    @Test
    public void testDeleteMessageUsingHash() throws Exception {
        String hash = new Message(ID_2, 1, RECIPIENT_2, MESSAGE_2).getMessageHash();
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                Message.deleteMessageByHash(hash));

        // The message is gone from the stored array ...
        assertEquals(Arrays.asList(MESSAGE_5), Message.getStoredMessageTexts());
        // ... and from the JSON file
        String json = Files.readString(jsonFile);
        assertFalse(json.contains("Where are you?"));
        assertTrue(json.contains("Ok, I am leaving without you."));
    }

    @Test
    public void testDeleteMessageHashNotFound() {
        assertEquals("No message was found with the message hash 00:0:NOPE.",
                Message.deleteMessageByHash("00:0:NOPE"));
    }

    // --- Display report ---

    @Test
    public void testSentMessagesReport() {
        String report = Message.sentMessagesReport();
        assertTrue(report.contains("Message Hash: 01:0:DIDCAKE"));
        assertTrue(report.contains("Recipient: " + RECIPIENT_1));
        assertTrue(report.contains("Message: " + MESSAGE_1));
        assertTrue(report.contains("Message Hash: 08:3:ITTIME"));
        assertTrue(report.contains("Message: " + MESSAGE_4));
        // Stored and disregarded messages are not in the sent report
        assertFalse(report.contains(MESSAGE_2));
        assertFalse(report.contains(MESSAGE_3));
    }

    @Test
    public void testStoredMessagesReportShowsFullDetails() {
        String report = Message.storedMessagesReport();
        assertTrue(report.contains("Message ID: " + ID_2));
        assertTrue(report.contains("Message Hash: 02:1:WHERETIME"));
        assertTrue(report.contains("Sender: Ora Smith"));
        assertTrue(report.contains("Recipient: " + RECIPIENT_2));
        assertTrue(report.contains("Message: " + MESSAGE_2));
        assertTrue(report.contains("Message: " + MESSAGE_5));
        assertFalse(report.contains(MESSAGE_1));
    }

    @Test
    public void testSenderAndRecipientOfStoredMessages() {
        String expected = "Sender: Ora Smith | Recipient: " + RECIPIENT_2 + System.lineSeparator()
                + "Sender: Ora Smith | Recipient: " + RECIPIENT_5;
        assertEquals(expected, Message.displaySenderAndRecipientOfStored());
    }

    // --- Reading the JSON file back into the stored array ---

    @Test
    public void testStoredMessagesReadBackFromJsonFile() {
        // Simulates closing and reopening the app: memory is wiped but the file stays
        Message.clearAll();
        assertEquals(0, Message.getStoredMessageTexts().size());

        assertEquals(2, Message.loadStoredMessages());
        assertEquals(Arrays.asList(MESSAGE_2, MESSAGE_5), Message.getStoredMessageTexts());
        assertEquals(Arrays.asList("02:1:WHERETIME", "05:4:OKYOU"), Message.getMessageHashes());
        assertEquals("Sender: Ora Smith | Recipient: " + RECIPIENT_2 + System.lineSeparator()
                + "Sender: Ora Smith | Recipient: " + RECIPIENT_5, Message.displaySenderAndRecipientOfStored());
    }

    @Test
    public void testLoadStoredMessagesWithNoFile() throws Exception {
        Message.clearAll();
        Files.delete(jsonFile);
        assertEquals(0, Message.loadStoredMessages());
    }

    @Test
    public void testMessageWithQuotesSurvivesJsonRoundTrip() {
        Message.clearAll();
        Message tricky = new Message("0412345678", 0, "+27831234567", "She said \"hi\" \\ and left");
        tricky.sentMessage(3);

        Message.clearAll();
        assertEquals(1, Message.loadStoredMessages());
        assertEquals("She said \"hi\" \\ and left", Message.getStoredMessageTexts().get(0));
    }
}
