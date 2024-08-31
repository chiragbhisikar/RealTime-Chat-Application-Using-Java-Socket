import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private int port;

    public Server(int port) {
        this.port = port;
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            pool = Executors.newCachedThreadPool();

            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);

                pool.execute(handler);
            }
        } catch (NullPointerException npe) {
            port = port + 1;
            run();
        } catch (Exception e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {

            }
        } catch (IOException e) {
            shutdown();
        }
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a nickname !");
                nickname = in.readLine();
                System.out.println(nickname + " connected!");
                broadcast(nickname + " joined a chat!");

                String message;
                while ((message = in.readLine()) != null) {
                    // change nickname code
                    if (message.startsWith("/nick ")) {
                        String[] messageSpilt = message.split(" ", 2);
                        if (messageSpilt.length == 2) {
                            broadcast(nickname + " rename him to " + messageSpilt[1]);
                            System.out.println(nickname + " rename him to " + messageSpilt[1]);
                            nickname = messageSpilt[1];
                            out.println("Successfully changed nickname to " + nickname);
                        } else {
                            out.println("no nickname provide!");
                        }
                        // For Quiting From The Chat
                    } else if (message.startsWith("/quit")) {
                        System.out.println(nickname + " disconnected!");
                        broadcast(nickname + " left the chat !");
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                if (!client.isClosed()) {
                    in.close();
                    out.close();
                    client.close();
                }
            } catch (IOException e) {
                System.out.println("Some Error Occurred When Client Exited From The Server!!");
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server(9999);
        server.run();
    }
}
