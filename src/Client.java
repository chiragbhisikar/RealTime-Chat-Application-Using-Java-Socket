import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private Thread thread;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            thread = new Thread(inHandler);
            thread.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    // code for existing from the chat and closing client socket
    public void shutdown() {
        done = true;
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (Exception e) {
            System.out.println("Some Error Occurred When Exiting!! => " + e);
        }
    }

    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                // taking input from the user
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));

                while (!done) {
                    String message = inReader.readLine();
                    // code for quitting from the chat
                    if (message.equals("/quit")) {
                        out.println(message);
                        inReader.close();
                        shutdown();
                    }
                    // code for sending message
                    else {
                        out.println(message);
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
