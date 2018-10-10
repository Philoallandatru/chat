import java.io.DataOutputStream;

/**
 * This class is designed to group all the signal handling stuff together,
 * The constructor is pretty strange,
 */
class ChatSignal {
    private String signal;
    private String error = "error"; // todo: this should be a number ...
    private String success = "success";
    private Boolean isValidChatSignal = false;
    private String userDoesntExist = "1123";
    private String willingToAcceptChatRequest = "1242";

    // now it is package-privagte
    ChatSignal(String message) {
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

    public String getErrorSignal() { return this.error; }

    public String getSuccessSignal() { return this.success; }

    public String getWillingToAcceptChatRequest() {return this.willingToAcceptChatRequest;}
    public String getUserDoesntExistSignal() {return this.userDoesntExist;}

    public boolean isValid() {return isValidChatSignal;}

    private static void handleWantToChatPersonally(DataOutputStream dout, ChatServer.ClientsInfo clientsInfo) {
        System.out.println("hello world");
    }

}