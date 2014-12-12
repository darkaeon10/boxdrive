package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

import serverjobs.ServerJobManager;
import commons.Constants;
import conn.ConnectionManager;

public class Server {
	private ServerSocket serverSocket;
	private ServerJobManager jobManager;
	
	public static void main(String args[]) {
		new Server(Paths.get("server"));
	}

	public Server(Path path) {
		Constants.FOLDER = path.toString();

		// ServerJobManager.getInstance().setFolder(FOLDER);

		// TODO: Change to ServerJobManager
		jobManager = new ServerJobManager();
		
		try {
			serverSocket = new ServerSocket(Constants.PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		acceptConnections();
	}
	
	protected void acceptConnections() {
		// maybe make a method sa connectionmanager for the serverjobmanager?
		
		/*
		while (true) {
			Socket newSocket = acceptNewConnection();
			if (newSocket != null)
				ConnectionManager.getInstance().createNewConnection(newSocket, jobManager);
			System.out.println(newSocket.getRemoteSocketAddress() + " has connected.");
		}
		*/
	}

	public Socket acceptNewConnection() {
		try {
			System.out.println("Waiting for connection.");
			Socket newSocket = serverSocket.accept();
			return newSocket;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Socket getSocket(int index) {
		return ConnectionManager.getInstance().getSocket(index);
	}

	public Socket getFirstSocket() {
		return getSocket(0);
	}
}
