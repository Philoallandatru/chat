import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

// todo: write comments and specifications for fucntions or statements
// todo: thread safety
// todo: encapsulation
// todo: rewrite this with MVC
// todo: provide function of disconnect
// todo: enable port number to be user-defined
// todo: history function should make a directory to store the chat history files.

public class ChatClient extends JFrame implements Runnable {
    public static void main(String[] args) {
        new ChatClient();
    }

    // Text field for chat
    private JTextField jtf = new JTextField();

    // Text field for name
    private JTextField jtfName = new JTextField("Enter a name");

    // Text area to display contents
    private JTextArea jta = new JTextArea();

    // Socket
    private Socket socket;

    // IO streams
    private DataOutputStream dout;
    private DataInputStream din;

    // Server Information
    private static String serverIP = "localhost";
    private static int serverPort;


    /**
     * initialize the GUI window,
     */
    private ChatClient() {
        // Panel p1 to hold the label and text field
        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());
        p1.add(new JLabel("Enter text"), BorderLayout.WEST);
        p1.add(jtf, BorderLayout.CENTER);

        // Panel p2 to hold the label and name field
        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        p2.add(new JLabel("Name"), BorderLayout.WEST);
        p2.add(jtfName, BorderLayout.CENTER);

        // Panel p to hold p1 and p2
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(p1, BorderLayout.SOUTH);
        p.add(p2, BorderLayout.NORTH);

        setLayout(new BorderLayout());
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(jta), BorderLayout.CENTER);

        jtf.addActionListener(new ButtonListener()); // Register listener

        jta.setEditable(false); // Disable editing of chat area

        setTitle("ChatClient");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // set menubar
        JMenuItem jmiViewHistory = new JMenuItem("View Chat History");
        JMenuItem jmiConnect = new JMenuItem("Connect to");
        JMenu connect = new JMenu("Connection");
        connect.add(jmiConnect);
        JMenu history = new JMenu("Histroy");
        history.add(jmiViewHistory);
        JMenuBar jmb = new JMenuBar();
        jmb.add(connect);
        jmb.add(history);
        this.setJMenuBar(jmb);

        // add action listener for menubar items
        jmiViewHistory.addActionListener(e -> viewHistory());
        jmiConnect.addActionListener(e -> connectToServer());

        setVisible(true); // It is necessary to show the frame here!

    }

    private void connect() {
        try {
            // Create a socket to connect to the server
            System.out.println(serverIP);
            socket = new Socket(serverIP, 8000);

            // Create an input stream to receive data from the server
            din = new DataInputStream(socket.getInputStream());

            // Create an output stream to send data to the server
            dout = new DataOutputStream(socket.getOutputStream());

            // Start a new thread for receiving messages
            new Thread(this).start();
        } catch (IOException ex) {
            jta.append(ex.toString() + '\n');
        }
    }
    /**
     * read the history file, show it in an option pane
     */
    private void viewHistory() {
        // create an output stream for the file we wanna read.
        StringBuilder history = new StringBuilder("");
        try {
            // read the history file
            String filename = jtfName.getText() + "-History.txt";
            BufferedReader fin = new BufferedReader(new FileReader(filename));
            String s;
            while ((s = fin.readLine()) != null) {
                history.append(s +'\n');
            }

            // test code
            System.out.println("I am writing History for " + jtfName.getText() + ":");
            System.out.println("Content is " + history + "\n\n");

            // create an option pane to show the history
            JScrollPane jspHistory = new JScrollPane(new JTextArea(history.toString()));
            JOptionPane.showMessageDialog(null, jspHistory,
                    "Chat History", JOptionPane.INFORMATION_MESSAGE, null);

        } catch (IOException ex) {
            System.err.println(ex);
        }
        System.out.println("hello world");
    }

    /**
     *
     */
    private void connectToServer() {
        System.out.println(serverIP);
        // System.out.println("Invoking connectToServer() method....");
        serverIP = JOptionPane.showInputDialog(null,
                "Server IP:",
                "Server IP Address", JOptionPane.QUESTION_MESSAGE).trim();
        System.out.println(serverIP);
        String portNumber = JOptionPane.showInputDialog(null,
                "Enter Server Port Number:", "Server Port Number", JOptionPane.QUESTION_MESSAGE).trim();
        // todo: use regex to be sure that they are legal
        this.connect();
    }

    private class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                // Get the text from the text field
                String string = jtfName.getText().trim() + ": " + jtf.getText().trim();

                // Send the text to the server
                dout.writeUTF(string);

                // Clear jtf
                jtf.setText("");
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }

    // Gets mesages from other clients
    public void run() {
        try {
            while (true) {
                // Get message
                String text = din.readUTF();

                // wtite chat history to a file
                String filename = jtfName.getText() + "-History.txt";
                FileWriter fw = new FileWriter(filename, true);
                fw.write(text+'\n');
                fw.close();

                // Display to the text area
                jta.append(text + '\n');
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
