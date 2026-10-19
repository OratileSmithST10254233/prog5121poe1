import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Message class (Part 2), using the test data
 * from the PROG5121POE brief.
 */
public class MessageTest {

    @TempDir
    Path tempDir;

    // Test Case 1 and Test Case 2 from the brief
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String MESSAGE_1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String MESSAGE_2 = "Hi Keegan, did you receive the payment?";

    private Path jsonFile;

    @BeforeEach
    public void setUp() {
        Message.clearAll();
        jsonFile = tempDir.resolve("storedMessages.json");
        Message.setStoragePath(jsonFile.toString());
    }

    // --- Message length (assertEquals: success and failure) ---

    @Test
    public void testMessageLengthSuccess() {
        Message message = new Message(0, RECIPIENT_1, MESSAGE_1);
        assertEquals("Message ready to send.", message.checkMessageLength());
    }

    @Test
    public void testMessageLengthExactly250IsAllowed() {
        Message message = new Message(0, RECIPIENT_1, "a".repeat(250));
        assertEquals("Message ready to send.", message.checkMessageLength());
    }

    @Test
    public void testMessageLengthFailure() {
        Message message = new Message(0, RECIPIENT_1, "a".repeat(260));
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                message.checkMessageLength());
    }

    // --- Recipient number (assertEquals: success and failure) ---

    @Test
    public void testRecipientCellSuccess() {
        Message message = new Message(0, RECIPIENT_1, MESSAGE_1);
        assertEquals("Cell phone number successfully captured.", message.checkRecipientCell());
    }

    @Test
    public void testRecipientCellFailure() {
        Message message = new Message(1, RECIPIENT_2, MESSAGE_2);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.",
                message.checkRecipientCell());
    }

    // --- Message hash ---

    @Test
    public void testMessageHashTestCase1() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        assertEquals("00:0:HITONIGHT", message.createMessageHash());
    }

    @Test
    public void testMessageHashesInALoop() {
        String[] ids = {"0012345678", "0198765432", "0012345678", "1234567890"};
        int[] numbers = {0, 1, 0, 3};
        String[] texts = {MESSAGE_1, MESSAGE_2, "Hi Mike, thanks!", "Hello"};
        String[] expected = {"00:0:HITONIGHT", "01:1:HIPAYMENT", "00:0:HITHANKS", "12:3:HELLOHELLO"};

        for (int i = 0; i < ids.length; i++) {
            Message message = new Message(ids[i], numbers[i], RECIPIENT_1, texts[i]);
            assertEquals(expected[i], message.createMessageHash());
        }
    }

    // --- Message ID ---

    @Test
    public void testMessageIDCreated() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        assertEquals("Message ID generated: 0012345678", message.messageIDGenerated());
    }

    @Test
    public void testGeneratedMessageIDIsTenDigits() {
        Message message = new Message(0, RECIPIENT_1, MESSAGE_1);
        assertTrue(message.checkMessageID());
        assertEquals(10, message.getMessageID().length());
    }

    @Test
    public void testMessageIDLongerThanTenFails() {
        Message message = new Message("12345678901", 0, RECIPIENT_1, MESSAGE_1);
        assertFalse(message.checkMessageID());
    }

    // --- MessageSent: send, disregard, store ---

    @Test
    public void testMessageSentSend() {
        Message message = new Message(0, RECIPIENT_1, MESSAGE_1);
        assertEquals("Message successfully sent.", message.sentMessage(1));
    }

    @Test
    public void testMessageSentDisregard() {
        Message message = new Message(1, RECIPIENT_2, MESSAGE_2);
        assertEquals("Press 0 to delete the message.", message.sentMessage(2));
    }

    @Test
    public void testMessageSentStore() {
        Message message = new Message(0, RECIPIENT_1, MESSAGE_1);
        assertEquals("Message successfully stored.", message.sentMessage(3));
    }

    // --- Total messages sent (test data: message 1 sent, message 2 discarded) ---

    @Test
    public void testReturnTotalMessagesWithTestData() {
        Message first = new Message(0, RECIPIENT_1, MESSAGE_1);
        Message second = new Message(1, RECIPIENT_2, MESSAGE_2);

        first.sentMessage(1);   // Select Send
        second.sentMessage(2);  // Select Discard

        assertEquals(1, Message.returnTotalMessages());
    }

    // --- Displaying messages ---

    @Test
    public void testPrintMessagesShowsDetailsInOrder() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        message.sentMessage(1);

        String output = Message.printMessages();
        int id = output.indexOf("0012345678");
        int hash = output.indexOf("00:0:HITONIGHT");
        int recipient = output.indexOf(RECIPIENT_1);
        int text = output.indexOf(MESSAGE_1);

        assertTrue(id >= 0 && id < hash && hash < recipient && recipient < text);
    }

    @Test
    public void testPrintMessagesWhenNothingSent() {
        assertEquals("No messages have been sent yet.", Message.printMessages());
    }

    // --- JSON storage ---

    @Test
    public void testStoredMessageIsWrittenToJson() throws IOException {
        Message message = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        message.sentMessage(3);

        assertTrue(Files.exists(jsonFile));
        String json = Files.readString(jsonFile, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"messageID\": \"0012345678\""));
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"recipient\": \"+27718693002\""));
    }

    @Test
    public void testJsonEscapesQuotes() throws IOException {
        Message message = new Message("0012345678", 0, RECIPIENT_1, "He said \"hi\" today");
        message.sentMessage(3);

        String json = Files.readString(jsonFile, StandardCharsets.UTF_8);
        assertTrue(json.contains("He said \\\"hi\\\" today"));
    }
}
