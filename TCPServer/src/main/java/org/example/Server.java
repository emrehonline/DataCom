package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final VendingMachine vendingMachine = new VendingMachine();

    public static void main(String[] args) {
        int port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                // Accept a new client connection
                Socket clientSocket = serverSocket.accept();

                // Start a new thread for each client
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }



    static private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {
                String message;
                String greetingMessage = "Welcome to the vending machine! \nAvailable commands:\n" +
                        "LIST - List all products\n" +
                        "BUY <product_name> - Buy a product(e.g. BUY Brownie)\n" +
                        "REFILL - Refill the vending machine\n" +
                        "EXIT - Exit";

                output.println(greetingMessage);

                // Continuously read messages from the client
                while ((message = input.readLine()) != null) {

                    String response = ProcessAndResponseToClientMessage(message);
                    output.println(response);

                    // IF user exit, close the connection
                    if (message.equalsIgnoreCase("EXIT")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Client error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }

    private static String ProcessAndResponseToClientMessage(String message){
        if (message.equalsIgnoreCase("LIST")) {
            return vendingMachine.displayProducts();
        } else if (message.startsWith("BUY")) {
            String[] parts = message.split(" ");
            if (parts.length == 3) {
                String productName = parts[1];
                String moneyStr = parts[2];
                double money = Double.parseDouble(moneyStr);
                return vendingMachine.buyProduct(productName, money);
            } else {
                return "Invalid command. Use BUY <product_name>";
            }
        } else if(message.equalsIgnoreCase("REFILL")){
            vendingMachine.refill();
            return "Vending Machine Refilled.";
        }else if (message.equalsIgnoreCase("EXIT")) {
            return "Thank you for using the vending machine!";
        } else {
            return "Unknown command.";
        }
    }
}