import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

// todo: write comments and specifications for fucntions or statements
// !!  todo: thread safety
// !!! todo: encapsulation
// todo: rewrite this with MVC
// todo: provide function of disconnect
// todo: enable port number to be user-defined
// todo: history function should make a directory to store the chat history files.
// todo: add help menu
// todo: are you sure to use regex to do it?????
// todo: add radio buttons
// !   todo:

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

    // Socket connected with Server
    private Socket socket;

    // IO streams
    private DataOutputStream dout;
    private DataInputStream din;

    // Server Information
    private static String serverIP = "localhost";
    private static int serverPort;

    // Client Status
    private static JRadioButton jrbStatusFree = new JRadioButton("Free", true);
    private static JRadioButton jrbStatusBusy = new JRadioButton("Busy", false);
    private static Boolean isFree = true;

    /**
     * Initialize the GUI window,
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

        // Panel p3 contains radio button to specify the status of the Client
        JPanel p3 = new JPanel();
        p3.setLayout(new BorderLayout());
        p3.add(jrbStatusFree, BorderLayout.WEST);
        p3.add(jrbStatusBusy, BorderLayout.CENTER);
        p2.add(p3, BorderLayout.EAST);

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
        JMenuItem jmiManual = new JMenuItem("Manual");
        JMenuItem jmiPersonallyChat = new JMenuItem("Chat to a Certain Person");
        JMenuItem jmiRegisterID = new JMenuItem("Register ID and Connect");

        JMenu connect = new JMenu("Connection"); // connect menu
        connect.add(jmiConnect);
        connect.add(jmiPersonallyChat);
        connect.add(jmiRegisterID);

        JMenu history = new JMenu("History"); // history menu
        history.add(jmiViewHistory);
        JMenuBar jmb = new JMenuBar();
        jmb.add(connect);
        jmb.add(history);
        JMenu jmHelp = new JMenu("Help"); // help menu
        jmb.add(jmHelp);
        this.setJMenuBar(jmb); // put this menubar on this frame

        // add action listener for menubar items
        jmiViewHistory.addActionListener(e -> viewHistory());
        jmiConnect.addActionListener(e -> connectToServer());
        jmiManual.addActionListener(e -> showHelpPane());
        jmiPersonallyChat.addActionListener(e -> personallyChat());
        jmiRegisterID.addActionListener(e -> registerID());
        jrbStatusBusy.addActionListener(e -> setStatusFree());
        jrbStatusBusy.addActionListener(e -> setStatusBusy());

        setVisible(true); // It is necessary to show the frame here!
    }

    // Bug here.....
    private void setStatusFree() {
        if (!jrbStatusFree.isSelected()) {
            jrbStatusFree.setSelected(true);
            if (jrbStatusBusy.isSelected()) jrbStatusBusy.setSelected(false);
            isFree = true;
        } else {
            jrbStatusFree.setSelected(false);
            if (!jrbStatusBusy.isSelected()) jrbStatusBusy.setSelected(true);
            isFree = false;
        }
        System.out.println("is Free:" + isFree);
    }

    /**
     *
     */
    private void setStatusBusy() {
        if (!jrbStatusBusy.isSelected()) {
           jrbStatusBusy.setSelected(true);
           if (jrbStatusFree.isSelected()) jrbStatusFree.setSelected(false);
           isFree = false;
        } else {
            jrbStatusBusy.setSelected(false);
            if (!jrbStatusFree.isSelected()) jrbStatusFree.setSelected(true);
            isFree = true;

        }
        System.out.println("is Free:" + isFree);
    }

    /**
     * Send the server a signal(///1111) - this signal is so ugly
     * Send the server your ID that you wanna register
     * Receive the message that whether the ID has been used.
     *      The will be a info OptionPane
     * Invoke the normal connect() function
     */
    private void registerID() {
        try {
            dout.writeUTF("///1111");
            String name = JOptionPane.showInputDialog(null,
                "Enter ID You Want to Register:", "Register ID and Connect",
                JOptionPane.QUESTION_MESSAGE);
            dout.writeUTF(name);
            String isNameUserd = din.readUTF();
            if (isNameUserd.equals("true")) JOptionPane.showMessageDialog(null, "This ID has been used.",
                        "Error", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

    /**
     * There are 2 two choice:
     *      1. Implement this with the server filtering the message for clients
     *          that want to chat personally, which involves that we have to implement
     *              1. a flag(or non-message information) processing mechanism
     *              2. Change the way we receiving and sending messages(this should be a heavy stuff)
     *      2. Give every client a choice to start its own server(which is really P2P-like) JUST TO CONNECT
     *          WITH A SINGLE PERSON. This make the code looks ugly, inefficent, akward, but much
     *          easier to be implemented.
     *
     * @throws IOException
     */
    private void personallyChat() {
        try {
            // open a window to get the name of the use as its identifier
            // send a certain message to the server that this is a special message
            // send the ID to the server
            // enter the ID your want to chat personally
            // send the ID to the server
            // the server searches it, if it doesn't find it, send the client something to
            // tell him that this man doesn't exitsts.
            // if the server finds it, just tell the client that it was fine, and the server start a mechanism that
            //
            String s = "I wanna talk to my love!";
            dout.writeUTF(s);
        } catch (IOException ex) {
            System.err.println(ex);

        }
    }

    private class Model {

    }

    private void showHelpPane() {

        JScrollPane jspHelp = new JScrollPane(new JTextArea());
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
    }


    /**
     *
     */
    private void connectToServer() {
        // System.out.println(serverIP);
        // System.out.println("Invoking connectToServer() method....");

        // System.out.println(serverIP);
        if (!(serverIP = JOptionPane.showInputDialog(null,
                "Server IP:", "Server IP Address",
                JOptionPane.QUESTION_MESSAGE).trim()).matches
                (("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)" +
                        "(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"))) {
            serverIP = "localhost";
            JOptionPane.showMessageDialog(null, "Address Illegal", "Error", JOptionPane.WARNING_MESSAGE);
        }

        String portNumber = JOptionPane.showInputDialog(null,
                "Enter Server Port Number:", "Server Port Number", JOptionPane.QUESTION_MESSAGE).trim();
        // todo: use regex to be sure that they are legal
        this.connect();
    }

    /**
     * TODO: make sure that the connection has been established.
     * Get the input and output stream here.
     */
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
