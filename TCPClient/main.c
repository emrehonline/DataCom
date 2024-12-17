#include <stdlib.h>
#include <stdio.h>
#include <winsock2.h>
#include <WS2tcpip.h>
#include <string.h>

#pragma comment(lib, "ws2_32.lib")

#define PORT 8080
#define BUFFER_SIZE 1024
#define IP "127.0.0.1"

int main(void) {

    struct sockaddr_in serv_addr;
    char buffer[BUFFER_SIZE];
    char message[BUFFER_SIZE];
    WSADATA wsadata;
    SOCKET clientSocket;

    char userName[BUFFER_SIZE];
    double userBudget = 20.0;
    do{
        // Get user input
        printf("Enter username: ");
        fgets(userName, BUFFER_SIZE, stdin);
    }while(userName[0] == '\n');
    userName[strcspn(userName, "\n")] = 0;  // Remove newline character
    printf("Hi %s, Your budget is: $%.2f\n\n", userName, userBudget);


    if(WSAStartup(MAKEWORD(2,2), &wsadata) != 0){
        printf("Failed to initialize Winsock. Error Code: %d\n", WSAGetLastError());
        return -1;
    }

    clientSocket = socket(AF_INET, SOCK_STREAM, 0);
    if(clientSocket == INVALID_SOCKET){
        printf("Could not create socket. Error Code: %d\n", WSAGetLastError());
        WSACleanup();
        return -1;
    }

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = inet_addr(IP);
    serv_addr.sin_port = htons(PORT);

    // Connect to the server
    if (connect(clientSocket, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0) {
        printf("Connection Failed. Please control the TCP server.\n");
        closesocket(clientSocket);
        WSACleanup();
        return -1;
    }

    // Receive and print the welcome message
    memset(buffer, 0, BUFFER_SIZE);
    recv(clientSocket, buffer, BUFFER_SIZE - 1, 0);
    printf("%s\n", buffer);

    while (1) {
        // Get user input
        printf("Enter command: ");
        fgets(message, BUFFER_SIZE, stdin);


        // If the command is a BUY command, add user budget at the end of the message
        if (strncmp(message, "BUY ", 4) == 0) {
            // Remove newline character
            message[strcspn(message, "\n")] = 0;
            // Append the budget to the message
            snprintf(message + strlen(message), BUFFER_SIZE - strlen(message), " %.2f", userBudget);
            // Add newline character to the message
            strcat(message, "\n");
        }

        // Send command to the server
        send(clientSocket, message, strlen(message), 0);

        // Exit if the user types "EXIT"
        if (strncmp(message, "EXIT", 4) == 0) {
            break;
        }

        // Receive server response
        memset(buffer, 0, BUFFER_SIZE);
        long bytesRead = recv(clientSocket, buffer, BUFFER_SIZE - 1, 0);;

        // Check for server disconnection
        if (bytesRead < 0) {
            // An error occurred
            perror("Error reading from socket");
            break; // Exit the loop if there is an error
        } else if (bytesRead == 0) {
            // Server has closed the connection
            printf("Server has disconnected.\n");
            break; // Exit the loop on disconnection
        }

        if (bytesRead > 0) {
            // Check if the response contains "Remaining money:"
            char *remainingMoneyPos = strstr(buffer, "Remaining money:");
            if (remainingMoneyPos) {
                char productName[BUFFER_SIZE]; // Make sure this is large enough to hold the product name

                // Find the position of the period
                char *periodPos = strchr(buffer, '.');

                if (periodPos != NULL) {
                    // Calculate the length of the product name
                    size_t responseLength = periodPos - buffer;

                    // Copy the product name into the productName variable and null-terminate it
                    strncpy(productName, buffer, responseLength);
                    productName[responseLength] = '\0'; // Null-terminate the string

                    // Print the product name
                    printf("%s\n", productName);
                }


                double remainingMoney;
                // Extract the remaining money from the response
                sscanf(remainingMoneyPos, "Remaining money: %lf", &remainingMoney);

                // Update userBudget
                userBudget = remainingMoney;
                printf("Remaining budget: $%.2f\n\n", userBudget);
            }else{
                printf("%s\n", buffer);
            }
        }
    }

    // Close the socket
    close(clientSocket);

    return 0;
}
