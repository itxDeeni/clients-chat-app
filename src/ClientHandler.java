import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

// runnable: instances will be executed by a separate thread
public class ClientHandler implements Runnable{

    /* list of every ClientHandler object that we instantiated
    * keeps track of all clients: to allow the message to be sent to everyone
    * static: belongs to the class, not to each object of the class*/
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    // passed from server class, used to establish connection between client and server
    private Socket socket;

    // reads messages sent from the client
    private BufferedReader bufferedReader;

    // sends messages received from other clients (to be broadcasted using clientHandlers
    private BufferedWriter bufferedWriter;

    private String clientUsername;

    ClientHandler(Socket socket) {
        // we use try because of IO exceptions
        try {
            this.socket = socket;
            /* each socket connection has an output stream and input stream to send and read data
            * java has a byte stream and a character stream (we want a character stream
            * character streams end with "Writer", byte stream end with "Stream"
            * we are sending over characters so we wrap the output stream*/
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            // we want to add the client to the static arraylist and pass this as ClientHandler object
            clientHandlers.add(this);
            Server.addChatMessage("SERVER: " + clientUsername + " has entered the chat!");
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /* contains what is run on a separate thread
    * part of Runnable implementation
    * we want to listen for messages (blocking operation - program will be stuck)
    * we will have a thread waiting for messages and another working with the rest of the application*/
    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                //listen for messages, halt until you receive a message
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient != null) {
                    Server.addChatMessage(clientUsername + ": " + messageFromClient);
                    broadcastMessage(messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void logMessage(String message) {
        try (FileWriter fileWriter = new FileWriter("chatlog.txt", true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(LocalDateTime.now() + " " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String messageToSend) {
        // clientHandler represents each ClientHandler in our arraylist
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
        logMessage(messageToSend);
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getClientUsername() {
        return clientUsername;
    }
}
