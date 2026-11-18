# prog5121poe1
# PROG5121 POE — Part 1: Registration and Login

Console-based Java application implementing user registration and login
validation for the PROG5121 chat app POE.

## Build & test
mvn test


# PROG5121 POE - Part 2: QuickChat Messaging Application

## Overview
This application is Part 2 of the PROG5121 POE assignment. It extends the Part 1 user authentication system (login and registration) by implementing a console-based messaging feature called **QuickChat**.

## Features
- **User Authentication:** Restricts message sending capabilities to successfully authenticated users.
- **Interactive Numeric Menu:**
  1. **Send Messages:** Prompts the user to enter recipients and messages, generates metadata, and tracks sent counts.
  2. **Show Recently Sent Messages:** Feature placeholder displaying `"Coming Soon."`
  3. **Quit:** Gracefully exits the application.
- **Message Validation & Processing:**
  - **Message Length Check:** Ensures messages do not exceed 250 characters.
  - **Recipient Formatting:** Validates international country code formatting.
  - **Tracking & ID Generation:** Automatically generates a unique 10-digit Message ID and tracks total messages sent.
  - **Message Hash:** Generates a custom hash formatted as `00:0:FIRSTWORDLASTWORD` in uppercase.
  - **Message Actions:** Allows users to Send, Disregard (Delete), or Store messages for later.

### Part 3: Data Arrays, Search & Reporting
- **Dynamic Array Management:** Populates and manages five parallel/structured arrays without hard-coding[cite: 19]:
  - `Sent Messages` — stores all sent messages[cite: 19].
  - `Disregarded Messages` — stores all disregarded messages[cite: 19].
  - `Stored Messages` — reads stored messages from the saved JSON file into memory[cite: 19].
  - `Message Hashes` — stores generated hashes for tracking and deletion[cite: 19].
  - `Message IDs` — stores unique 10-digit message identifiers[cite: 19].
- **Stored Messages & Reporting Menu:**
  - Display the sender and recipient of all stored messages[cite: 19].
  - Identify and display the longest stored/sent message[cite: 17, 19].
  - Search for a specific **Message ID** and return the corresponding recipient and message[cite: 19].
  - Search and display all messages sent or stored for a specific recipient number[cite: 17, 19].
  - Delete a message from the records using its **Message Hash**[cite: 17, 19].
  - Generate and display a full **Sent Messages Report** listing the Message Hash, Recipient, and Message for all sent items.

---