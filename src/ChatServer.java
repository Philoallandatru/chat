import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer extends JFrame {
    // Text area for displaying contents
    private JTextArea jta = new JTextArea();
    // Mapping of sockets to output streams
    private Hashtable outputStreams = new Hashtable();

    // Mapping of socket to id
    private static Map<Socket, String> idOfSocket = new HashMap<>();

    // Server socket
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        new ChatServer();
    }

    private ChatServer() {
        // Place text area on the frame
        setLayout(new BorderLayout());
        add(new JScrollPane(jta), BorderLayout.CENTER);

        setTitle("Multi-User Chat Server");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true); // It is necessary to show the frame here!

        jta.setEditable(false); // Disable editing of server log

        // Listen for connections
        listen();
    }


    /**
     * listen for a connection, create a mapping socket --> output stream
     * to write message to it.
     */
    private void listen() {
        try {
            // Create a server socket
            serverSocket = new ServerSocket(8000);
            jta.append("MultiThreadServer started at " + new Date() + '\n');

            while (true) {
                // Listen for a new connection request, this is a blocking function
                Socket socket = serverSocket.accept();

                // Display the client number
                jta.append("Connection from " + socket + " at " + new Date() + '\n');

                // Create output stream
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());

                // Save output stream to hashtable
                outputStreams.put(socket, dout);

                // Create a new thread for the connection, this thread
                new ServerThread(this, socket);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    // Used to get the output streams
    private Enumeration getOutputStreams() {
        return outputStreams.elements();
    }

    // Used to send message to all clients
    private void sendToAll(String message) {
        // Go through hashtable and send message to each output stream
        for (Enumeration e = getOutputStreams(); e.hasMoreElements(); ) {
            DataOutputStream dout = (DataOutputStream) e.nextElement();
            try {
                // Write message
                dout.writeUTF(message);
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }


    /**
     *
     */
    class ServerThread extends Thread {
        private ChatServer server;
        private Socket socket;

        /**
         * Construct a thread
         */
        public ServerThread(ChatServer server, Socket socket) throws IOException {
            this.socket = socket;
            this.server = server;

            start();
        }

        /**
         * Run a thread
         */
        public void run() {
            try {
                // Create data input and output streams
                DataInputStream din = new DataInputStream(socket.getInputStream());

                // Continuously serve the client
                while (true) {
                    // read incoming message
                    String message = din.readUTF();

                    DataOutputStream history = new DataOutputStream(new FileOutputStream("chatHistory.dat", true));
                    history.writeUTF(message);

                    // Send text back to all the clients
                    server.sendToAll(message);

                    // Add chat to the server jta
                    jta.append(message + '\n');
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }
}
