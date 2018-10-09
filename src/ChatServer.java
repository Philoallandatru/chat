import com.sun.xml.internal.bind.v2.model.core.ID;

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
    private Hashtable outputStreams = new Hashtable(); // this should be included in the ClientInfo class

    // Mapping of socket to id
    private static Map<Socket, String> idOfSocket = new HashMap<>();

    ClientsInfo clientsInfo = new ClientsInfo();
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

    class ClientsInfo {

        private Map<Socket, String> IDofSocket = new HashMap<>();


        // todo: you seems to need this one only(instead of one above)
        // but there is not enough time for me correct it???
        private Map<String, Socket> socketOfID = new HashMap<>();


        private Map<String, Boolean> isUserFreeToChatTo = new HashMap<>();
        private Map<String, String> thisWantsToChatToThat = new HashMap<>();

        public ClientsInfo() { }

        public void registerAnID(Socket socket, String id) {
            IDofSocket.put(socket, id);
            socketOfID.put(id, socket);
        }

        public boolean hasClientRegistered(Socket socket) {
            return this.IDofSocket.containsKey(socket);
        }

        public boolean isIDUsed(String name) {
            return socketOfID.containsKey(name);
        }

        public Socket getSocketOfID(String name) {
            // todo : this is dangerous!!!!
            return socketOfID.get(name);
        }

        public String getIDofSocket(Socket socket) {
            return IDofSocket.get(socket);
        }

        @Override
        public String toString() {
            // todo: reimplement this toString
            return IDofSocket.toString();
        }
    }


    /**
     *
     */
    class ServerThread extends Thread {
        private ChatServer server;
        private Socket socket;
        private DataInputStream din;
        private DataOutputStream dout;

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
                din = new DataInputStream(socket.getInputStream()); // Create data input and output streams

                // Continuously serve the client
                while (true) {
                    String message = din.readUTF(); // read incoming message

                    // DataOutputStream history = new DataOutputStream(new FileOutputStream("chatHistory.dat", true));
                    // history.writeUTF(message);
                    // todo: if this message is a signal, do something else
                    if (message.startsWith("///")) { // is a signal
                        processSignal(message);
                    }

                    server.sendToAll(message); // Send text back to all the clients

                    jta.append(message + '\n'); // Add chat to the server jta
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        private void processSignal(String message) throws IOException {
            String signal = message.substring(3);
            ChatSignal mSignal = new ChatSignal(message);
            try {
                 // todo: we should have a HashMap that records: Signal Number ---> Signal Event
                if (signal.equals("1111")) { // the client wanna register with its name
                    String registerName = din.readUTF();

                    boolean isNameUsedAlready = false;
                    if (clientsInfo.isIDUsed(registerName)) isNameUsedAlready = true; // whether this name has been used
                    else clientsInfo.registerAnID(socket, registerName); // store is with certain socket;

                    dout = new DataOutputStream(socket.getOutputStream());
                    dout.writeUTF("" + isNameUsedAlready);
                    System.out.println(idOfSocket);

                }
                if (signal.equals("2222")) { // find a friend to chat
                    if (clientsInfo.hasClientRegistered(socket))
                        dout.writeUTF(mSignal.getErrorSignal()); // first send the client a message
                    else dout.writeUTF(mSignal.getSuccessSignal());

                    // ask the name of the user you wanna chat to
                    String chatToWhom = din.readUTF();
                    if (clientsInfo.isIDUsed(chatToWhom)) dout.writeUTF(mSignal.getSuccessSignal());
                    else dout.writeUTF(mSignal.getErrorSignal()); // todo : should be user not found

                    // ash the user one client choose whether he or she is willing to chat
                    DataOutputStream askHimWillingOrNotToChat =
                            new DataOutputStream(clientsInfo.getSocketOfID(chatToWhom).getOutputStream());
                    askHimWillingOrNotToChat.writeUTF("Are you willing to chat with " + clientsInfo.getIDofSocket(socket));

                }
            } catch (IOException ex) {
                throw new IOException();
            }
        }

        // todo : maybe we caould consider the Signal as a class

        /**
         * This class is designed to group all the signal handling stuff together,
         * The constructor is pretty strange,
         */
        class ChatSignal {
            private String signal;
            private String error = "error"; // todo: this should be a number ...
            private String success = "success";
            private Boolean isValidChatSignal = false;
            public ChatSignal(String message) {
                if (message.startsWith("///")) {
                    // todo: as I say, there should be a container to store all the valid signal numbers
                    // do it here to judge whether it is valid
                    if (message.length() == 7) {
                        isValidChatSignal = true;
                    }
                }
                signal = message.substring(3);
            }

            public void handleThisSignal() {
                // todo: also use a hashtable to choose the function to something according to the signal number

            }

            public String getErrorSignal() {
                return this.error;
            }

            public String getSuccessSignal() {
                return this.success;
            }
        }
    }
}
