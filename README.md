# NFC Card Reader

This is an Android application for reading, writing, and formatting Mifare Classic NFC cards. It is built using Kotlin and Jetpack Compose.

## Features

*   **Read NFC Cards:** Authenticates and reads data from 9 specific fields on Mifare Classic cards.
*   **Write to NFC Cards:** Writes text data to the 9 fields on the card.
*   **Format NFC Cards:** Erases all data on the card by writing zeros to all writable blocks.
*   **AES Encryption:** Includes utilities for AES encryption and decryption (using BouncyCastle).

## Technical Details

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **NFC Technology:** Mifare Classic
- **Encryption:** AES (CBC mode with PKCS7Padding) provided by BouncyCastle.

## Key Mappings

The application uses the default Mifare Classic key for authentication.
Data is stored in specific blocks across multiple sectors.

## Getting Started

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Build and run the application on an Android device with NFC support.

## License

This project is licensed under the MIT License.
