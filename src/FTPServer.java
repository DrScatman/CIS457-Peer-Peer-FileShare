import java.net.ServerSocket;
import java.net.Socket;

class FTPServer extends Thread{

	@Override
	public void run() {
		ServerSocket welcomeSocket = null;
		try {
			welcomeSocket = new ServerSocket(8080);

		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			ClientHandler handler = new ClientHandler(connectionSocket);
			handler.start();
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}