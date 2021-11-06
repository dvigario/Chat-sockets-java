import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerSide {
	
	public static void main(String[] args) {
		
		ServerSocket server = null;
		try {
			System.out.println("starting the server");
			// create a ServerSocket class with port 999 parameter to start a server and accept new connections			
			server = new ServerSocket(9999);
			System.out.println("server started...");
			
			// while to accept connections
			while(true){
				Socket client = server.accept();  
				// waits for clients to connect and every time they connect comes a socket for me
				// but not to do it all in here, we created a class to manage the socket server (next)
				// to manage the conversation between client and server
				new ClientManager(client);  // every time you have a new connection create a new client manager by passing the socket
			}
			
		} catch (IOException e) {
			
			try {
				if(server != null)
					server.close();
			} catch (IOException e1) {}
			
			System.err.println("port is busy or server has been closed");
			e.printStackTrace();
		}
		
	}
}
