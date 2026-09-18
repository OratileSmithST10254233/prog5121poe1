import java.util.regex.Pattern;

/**
 * Login class for the PROG5121 POE chat application — Part 1
 * (Registration and login feature).
 *
 * Implements the methods required by the brief:
 * checkUserName(), checkPasswordComplexity(), checkCellPhoneNumber(),
 * registerUser(), loginUser(), returnLoginStatus().
 */
public class Login {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String cellPhoneNumber;
    private boolean registered = false;

    public Login() {
    }

    // ---------------------------------------------------------------
    // Validation methods
    // ---------------------------------------------------------------

    /**
     * Username must contain an underscore and be no more than
     * five characters long.
     */
    public boolean checkUserName(String username) {
        if (username == null) {
            return false;
        }
        return username.contains("_") && username.length() <= 5;
    }

    /**
     * Password must be at least eight characters long and contain
     * a capital letter, a number, and a special character.
     */
    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasCapital = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasNumber = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecialChar = Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();
        return hasCapital && hasNumber && hasSpecialChar;
    }

    /**
     * Cell phone number must contain the international country code
     * (e.g. +27) followed by the number, with the total value being
     * a '+' plus 10-11 digits (matches the brief's test data:
     * "+27838968976" -> true, "08966553" -> false).
     */
    public boolean checkCellPhoneNumber(String cellPhoneNumber) {
        if (cellPhoneNumber == null) {
            return false;
        }
        return Pattern.matches("^\\+[0-9]{10,11}$", cellPhoneNumber);
    }

    // ---------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------

    /**
     * Registers a user if the username, password and cell phone
     * number are all correctly formatted. Returns the exact
     * messaging required by the brief.
     */
    public String registerUser(String firstName, String lastName, String username,
                                String password, String cellPhoneNumber) {

        if (!checkUserName(username)) {
            return "Username is not correctly formatted; please ensure that your username "
                    + "contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the password "
                    + "contains at least eight characters, a capital letter, a number, and a special character.";
        }
        if (!checkCellPhoneNumber(cellPhoneNumber)) {
            return "Cell phone number is incorrectly formatted or does not contain an "
                    + "international code; please correct the number and try again.";
        }

        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.cellPhoneNumber = cellPhoneNumber;
        this.registered = true;

        return "Username successfully captured. Password successfully captured. "
                + "Cell phone number successfully added. The user has been registered successfully.";
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------

    /**
     * Verifies that the entered username and password match the
     * details captured at registration.
     */
    public boolean loginUser(String username, String password) {
        return registered
                && this.username != null && this.username.equals(username)
                && this.password != null && this.password.equals(password);
    }

    /**
     * Returns the messaging required to verify the user's
     * authentication status.
     */
    public String returnLoginStatus(String username, String password) {
        if (loginUser(username, password)) {
            return "Welcome " + firstName + ", " + lastName + " it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }

    // ---------------------------------------------------------------
    // Getters (useful for the console app / tests)
    // ---------------------------------------------------------------

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isRegistered() {
        return registered;
    }
}