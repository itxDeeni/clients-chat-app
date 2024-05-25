// listens to clients who wish to connect
//creates new thread to handle the new connection

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {

        try {

            //server must run indefinitely
            while (!serverSocket.isClosed()) {

                //accept is a blocking method: program will be halted until a client connects
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");

                /* each object of this class will be responsible for communicating with the client;
                implements runnable
                runnable is implemented on a class whose instances will each be executed by a separate thread
                allows application to handle more clients at a time */
                ClientHandler clientHandler = new ClientHandler(socket);

                /* to spawn a new thread, we must create a thread object and pass in our object
                * object class must implement runnable*/
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* handles errors to avoid nested try/catches
    */
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
