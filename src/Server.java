    // listens to clients who wish to connect
    //creates new thread to handle the new connection

    import java.io.IOException;
    import java.net.ServerSocket;
    import java.net.Socket;
    import java.util.Scanner;
    import com.sun.net.httpserver.HttpServer;
    import com.sun.net.httpserver.HttpHandler;
    import com.sun.net.httpserver.HttpExchange;
    import java.io.IOException;
    import java.io.OutputStream;
    import java.net.InetSocketAddress;
    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;


    public class Server {

         private final ServerSocket serverSocket;
        private static final List<String> chatHistory = new ArrayList<>();


        public Server(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void startServer() {

            try {

                startHttpServer();
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

        private void startHttpServer() throws IOException {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
            httpServer.createContext("/", new InfoHandler());
            httpServer.setExecutor(null);
            httpServer.start();
            System.out.println("HTTP server started on port 8000");
        }


        public static synchronized void addChatMessage(String message) {
            chatHistory.add(message);
        }

        static class InfoHandler implements HttpHandler {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = generateResponse();
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

            private String generateResponse() {
                StringBuilder response = new StringBuilder();
                response.append("<html><body>");
                response.append("<h1>Server Information</h1>");
                response.append("<p>Current Time: ").append(LocalDateTime.now()).append("</p>");
                response.append("<h2>Connected Users:</h2><ul>");

                for (ClientHandler clientHandler : ClientHandler.clientHandlers) {
                    response.append("<li>").append(clientHandler.getClientUsername()).append("</li>");
                }
                response.append("</ul>");
                response.append("<h2>Chat History:</h2><ul>");

                synchronized (chatHistory) {
                    for (String message : chatHistory) {
                        response.append("<li>").append(message).append("</li>");
                    }
                }
                response.append("</ul>");
                response.append("</body></html>");
                return response.toString();
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


//        public static void main(String[] args) throws IOException {
//            ServerSocket serverSocket = new ServerSocket(1234);
//            Server server = new Server(serverSocket);
//            server.startServer();
//        }

        public static void main(String[] args) throws IOException {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Enter port for the server to listen on (default is 1234): ");
            String portInput = scanner.nextLine();
            int port;
            if (portInput.isEmpty()) {
                port = 1234;
            } else {
                port = Integer.parseInt(portInput);
            }

            ServerSocket serverSocket = new ServerSocket(port);
            Server server = new Server(serverSocket);
            server.startServer();
        }
    }
