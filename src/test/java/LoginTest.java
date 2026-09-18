import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Login class, using the exact test data
 * given in the PROG5121POE Part 1 brief.
 */
public class LoginTest {

    private Login login;

    @BeforeEach
    public void setUp() {
        login = new Login();
    }

    // --- checkUserName ---

    @Test
    public void testUsernameCorrectlyFormatted() {
        // Test Data: "kyl_1" -> contains underscore, <= 5 chars
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    public void testUsernameIncorrectlyFormatted() {
        // Test Data: "kyle!!!!!!!"
        assertFalse(login.checkUserName("kyle!!!!!!!"));
    }

    // --- checkPasswordComplexity ---

    @Test
    public void testPasswordMeetsComplexityRequirements() {
        // Test Data: "Ch&&sec@ke99!"
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    public void testPasswordDoesNotMeetComplexityRequirements() {
        // Test Data: "password"
        assertFalse(login.checkPasswordComplexity("password"));
    }

    // --- checkCellPhoneNumber ---

    @Test
    public void testCellPhoneNumberCorrectlyFormatted() {
        // Test Data: "+27838968976"
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    public void testCellPhoneNumberIncorrectlyFormatted() {
        // Test Data: "08966553"
        assertFalse(login.checkCellPhoneNumber("08966553"));
    }

    // --- registerUser (assertEquals on exact messaging) ---

    @Test
    public void testRegisterUser_UsernameIncorrect() {
        String result = login.registerUser("Kyle", "Test", "kyle!!!!!!!", "Ch&&sec@ke99!", "+27838968976");
        assertEquals(
            "Username is not correctly formatted; please ensure that your username "
                    + "contains an underscore and is no more than five characters in length.",
            result
        );
    }

    @Test
    public void testRegisterUser_PasswordIncorrect() {
        String result = login.registerUser("Kyle", "Test", "kyl_1", "password", "+27838968976");
        assertEquals(
            "Password is not correctly formatted; please ensure that the password "
                    + "contains at least eight characters, a capital letter, a number, and a special character.",
            result
        );
    }

    @Test
    public void testRegisterUser_CellPhoneIncorrect() {
        String result = login.registerUser("Kyle", "Test", "kyl_1", "Ch&&sec@ke99!", "08966553");
        assertEquals(
            "Cell phone number is incorrectly formatted or does not contain an "
                    + "international code; please correct the number and try again.",
            result
        );
    }

    @Test
    public void testRegisterUser_Success() {
        String result = login.registerUser("Kyle", "Test", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertEquals(
            "Username successfully captured. Password successfully captured. "
                    + "Cell phone number successfully added. The user has been registered successfully.",
            result
        );
    }

    // --- loginUser / returnLoginStatus (assertTrue/False) ---

    @Test
    public void testLoginSuccessful() {
        login.registerUser("Kyle", "Test", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    public void testLoginFailed() {
        login.registerUser("Kyle", "Test", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertFalse(login.loginUser("kyl_1", "WrongPassword1!"));
    }

    @Test
    public void testReturnLoginStatus_Success() {
        login.registerUser("Kyle", "Test", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertEquals(
            "Welcome Kyle, Test it is great to see you again.",
            login.returnLoginStatus("kyl_1", "Ch&&sec@ke99!")
        );
    }

    @Test
    public void testReturnLoginStatus_Failure() {
        login.registerUser("Kyle", "Test", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertEquals(
            "Username or password incorrect, please try again.",
            login.returnLoginStatus("kyl_1", "WrongPassword1!")
        );
    }
}