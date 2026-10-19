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

## Application Architecture