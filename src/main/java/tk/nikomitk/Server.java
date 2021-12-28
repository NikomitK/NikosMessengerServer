package tk.nikomitk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final String EMPTY_RESPONSE = "empty";
    private static ObjectMapper objectMapper;
    private static List<Message> messageList;
    private static List<User> userList;

    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");
        objectMapper = new ObjectMapper();
        messageList = new ArrayList<>();
        userList = new ArrayList<>();
        ServerSocket serverSocket = new ServerSocket(6969);
        while (true) {
            try {
                Socket client = serverSocket.accept();
                System.out.println(client.getInetAddress() + " connected");
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter toClient = new PrintWriter(client.getOutputStream(), true);
                String message = fromClient.readLine();
                System.out.println("received: " + message);
                String response = handle(objectMapper.readTree(message));

                if (!response.equals(EMPTY_RESPONSE)) {
                    toClient.println(response);
                }
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Client closed connection to early");
            }
        }
    }

    private static String handle(JsonNode request) throws JsonProcessingException {
        switch (request.get("type").asText()) {
            case "request":
                return thisIsAMessageRequest(request);
            case "login":
                return thisIsALogIn(request);
            case "signup":
                return thisIsASignUp(request);
            case "user":
                return thisIsAUserRequest(request);
            case "message":
                return thisIsAMessage(request);
        }
        return EMPTY_RESPONSE;
    }

    private static String thisIsAMessageRequest(JsonNode request) throws JsonProcessingException {
        String username = request.get("username").asText();
        List<Message> messages = new ArrayList<>();

        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getReceiver().equals(username)) {
                messages.add(messageList.get(i));
                messageList.remove(i);
                i--;
            }
        }
        if (messages.isEmpty()) return EMPTY_RESPONSE;
        return objectMapper.writeValueAsString(messages);
    }

    private static String thisIsALogIn(JsonNode request) {
        String username = request.get("username").asText();
        String password = request.get("hashedPassword").asText();
        for (User u : userList) {
            if (u.getUserName().equals(username) && u.getPassword().equals(password)) return "true";
        }
        return "false";
    }

    private static String thisIsASignUp(JsonNode request) {
        String username = request.get("username").asText();
        String password = request.get("hashedPassword").asText();
        User u = new User(username, 1);
        u.setPassword(password);
        userList.add(u);
        return "true";
    }

    private static String thisIsAUserRequest(JsonNode request) throws JsonProcessingException {
        String username = request.get("username").asText();
        User requestedUser = null;
        for (User u : userList) {
            if (u.getUserName().equals(username)) requestedUser = u;
        }
        if (requestedUser == null) return EMPTY_RESPONSE;
        return objectMapper.writeValueAsString(requestedUser);
    }

    private static String thisIsAMessage(JsonNode request) throws JsonProcessingException {
        String username = request.get("username").asText(); // Would've been a thing with encryption, but again, implementing that is way above the goal of this
        String thisMessageTree = request.get("encryptedText").asText();
        JsonNode thisMessageNode = objectMapper.readTree(thisMessageTree);
        Message thisMessage = objectMapper.treeToValue(thisMessageNode, Message.class);
        messageList.add(thisMessage);
        return EMPTY_RESPONSE;
    }
}
