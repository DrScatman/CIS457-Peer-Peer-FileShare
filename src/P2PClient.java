import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class P2PClient extends Thread {

    private Socket socket;
    private ObjectInputStream in;
    private DataOutputStream out;
    //    private boolean connectedToCentralServer;
    private String searchResponse;
    private FTPServer ftpServer;
    private HashSet<Peer> peerSet;
    private boolean searchCommandSent;
    private static final String FILE_LIST_FILENAME = "filelist.txt";
    private FTPClient ftpClient;

    /**
     * Commands that are sent to the client handler
     **/
    String connectCommand;
    String disconnectCommand;
    String searchCommand;
    private String FTPCommand;
    String newFileCommand;
    String commandline;


    public P2PClient(String serverHostName, int port) {
        try {
            socket = new Socket(serverHostName, port);
            //in = new DataInputStream(socket.getInputStream());
            in = new ObjectInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            peerSet = new HashSet<>();

            ftpServer = new FTPServer();
            ftpServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try {

                if (connectCommand != null && !connectCommand.isEmpty()) {
                    sendConnectCommand(connectCommand);
                    System.out.println("Connect command sent to:  " + socket.getInetAddress().getHostAddress());
                    connectCommand = null;
                }
                // sends a quit message to central server.
                if (disconnectCommand != null && !disconnectCommand.isEmpty()) {
                    //sendDisconnectCommand(disconnectCommand);

                    disconnectCommand = null;
                }

                if (newFileCommand != null && !newFileCommand.isEmpty()) {
                    newFileCommand = null;
                }
   ////FIX this  should go to FTP server of peer who has a file you want
                if (FTPCommand != null && !FTPCommand.isEmpty()) {
                    System.out.println("Sending: " + FTPCommand + "To Central Server");
                    sendFTPCommand(FTPCommand);
                    FTPCommand = null;
                }

                if (searchCommand != null && !searchCommand.isEmpty()) {
                   // sendSearchCommand(searchCommand);
                    searchCommandSent = true;
                    searchCommand = null;
                }

                if (searchCommandSent) {
                    boolean EOF = false;
                    while (!EOF) {
                        try {
                            Peer peer = (Peer) in.readObject();
                            if (peer != null) {
                                peerSet.add(peer);
                            }
                        } catch (EOFException e) {
                            EOF = true;
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    searchCommandSent = false;
                }



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Disconnected");
    }

    //needs to get list of clients and their files from the server
    //needs to have FTP client and server implemented, so detects connection and chooses which to be
    //always listening on port 8080 after connecting to server
    //has to send list of clients, and their files, and commands to GUI
    //has to receive commands from the gui for connecting to server and other peers
    //needs to upload file list and descriptions

    // Needs to send to CentralServer somewhere
    public void sendConnectCommand(String command) throws IOException {
        connectCommand = "newPeer " + command + "\r\n";
        out.writeBytes(connectCommand);
    }

    // Needs to send to CentralServer somewhere
    public void sendDisconnectCommand(String command) throws IOException {
        disconnectCommand = command + InetAddress.getLocalHost().getHostAddress() + "\r\n";
        out.writeBytes(disconnectCommand);
        System.out.println("Disconnect command sent to:  " + socket.getInetAddress().getHostAddress());
        out.close();
        in.close();
    }

    // Needs to send to CentralServer somewhere
    public void sendFTPCommand(String command) throws IOException {
        try {
            if (ftpClient == null) {
                ftpClient = new FTPClient(command, 8080);

            } else {
                ftpClient.sendCommand(command);
                ftpClient.run();
                commandline = ftpClient.sendySentence;
                ftpClient.sendySentence = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FTPCommand = command;
    }

    public HashSet<Peer> loadPeerList() {
        return peerSet;
    }

    public String sendCommandLine() {
        //commandline = ftpClient.sendySentence;
        return commandline;
    }

    public void sendSearchCommand(String command) {
        try {
            System.out.println("Searching for: " + searchCommand);
            searchCommand = "search " + command + "\r\n";
            searchCommandSent = true;
            out.writeBytes(searchCommand);
            searchCommand = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNewFileCommand(String command) throws IOException {
        newFileCommand = "200 " + command + "\r\n";
        out.writeBytes(newFileCommand);
        System.out.println("Files sent to:  " + socket.getInetAddress().getHostAddress());
    }

    public void loadSearchResults() {
        /*BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
        searchResponse = inData.readLine();*/
    }
}
