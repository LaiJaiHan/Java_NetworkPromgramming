import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import java.io.*;

public class ChatServerThread implements Runnable {

	KeyStore keyStore;
	KeyManagerFactory kManagerFactory;
	SSLContext sslContext;
	SSLServerSocketFactory sslServerSocketFactory = null;
	SSLServerSocket sslServerSocket = null;

	private RmiChatApplication rmiApp = null;

	private ChatServerThreadRunnable clients[] = new ChatServerThreadRunnable[3];
	public int clientCount = 0;

	private int ePort = -1;

	// for RMI
	private String server;
	private String serviceName;

	public ChatServerThread(int port, String server, String serviceName) {
		this.ePort = port;
		this.server = server;
		this.serviceName = serviceName;
	}

	public void run() {

		String ksName = "/Users/ijaehan/Desktop/SSLChatService/bin/keystore/SSLSocketServerKey";

		char keyStorePass[] = "jwlee9112405".toCharArray();
		char keyPass[] = "jwlee9112405".toCharArray();

		// set the key
		try {

			keyStore = KeyStore.getInstance("JKS");
			try {
				keyStore.load(new FileInputStream(ksName), keyStorePass);
			} catch (Exception e) {
				System.err.print("generate key first.");
			}

			kManagerFactory = KeyManagerFactory.getInstance("SunX509");
			kManagerFactory.init(keyStore, keyPass);

			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kManagerFactory.getKeyManagers(), null, null);

		} catch (Exception e) {
			System.out.println("key exception anyway, wrong:( ");
		}

		try {

			sslServerSocketFactory = sslContext.getServerSocketFactory();
			sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(ePort);

			System.out.println("Server started: SSLsocket created on " + ePort);

			// register it own rmiServer
			try {
				rmiApp = (RmiChatApplication) Naming.lookup("rmi://" + server + "/" + serviceName);
			} catch (NotBoundException e) {
				e.printStackTrace();
			}

			while (true) {
				addClient(sslServerSocket);
			}

		} catch (BindException b) {
			System.out.println("Can't bind on: " + ePort);
		} catch (IOException i) {
			System.out.println(i);
		} finally {
			try {
				if (sslServerSocket != null)
					sslServerSocket.close();
			} catch (IOException i) {
				System.out.println(i);
			}
		}
	}

	public int whoClient(int clientID) {

		for (int i = 0; i < clientCount; i++)
			if (clients[i].getClientID() == clientID)
				return i;
		return -1;

	}

	public void putClient(int clientID, String inputLine) {

		for (int i = 0; i < clientCount; i++)
			if (clients[i].getClientID() == clientID) {
				System.out.println("writer: " + clientID);
			} else {
				System.out.println("write: " + clients[i].getClientID());
				clients[i].out.println(inputLine);
			}

		try {
			rmiApp.auto(inputLine);
		} catch (RemoteException e) { 
			e.printStackTrace();
		}
	}

	public void addClient(ServerSocket sslServerSocket) {
		
		SSLSocket clientSocket = null;
		
		if (clientCount < clients.length) { 

			try {
				// generating client socket
				clientSocket = (SSLSocket) sslServerSocket.accept();
			} catch (IOException i) {

				System.out.println ("Accept() fail: "+i);

			}

			clients[clientCount] = new ChatServerThreadRunnable(this, clientSocket);
			new Thread(clients[clientCount]).start();

			clientCount++;
			System.out.println ("Client connected: " + clientSocket.getPort()
					+", CurrentClient: " + clientCount);

		} else {

			try {

				SSLSocket dummySocket = (SSLSocket) sslServerSocket.accept();

				ChatServerThreadRunnable dummyRunnable = new ChatServerThreadRunnable(this, dummySocket);
				new Thread(dummyRunnable);

				dummyRunnable.out.println(dummySocket.getPort()
						+ " < Sorry maximum user connected now");
				System.out.println("Client refused: maximum connection "
						+ clients.length + " reached.");
				dummyRunnable.close();

			} catch (IOException i) {

				System.out.println(i);

			}	
		}
	}
	
	public synchronized void delClient(int clientID) {

		int pos = whoClient(clientID);
		ChatServerThreadRunnable endClient = null;
	      if (pos >= 0) {
	    	   endClient = clients[pos];
	    	  if (pos < clientCount-1)
	    		  for (int i = pos+1; i < clientCount; i++)
	    			  clients[i-1] = clients[i];
	    	  clientCount--;
	    	  System.out.println("Client removed: " + clientID
	    			  + " at clients[" + pos +"], CurrentClient: " + clientCount);
	    	  endClient.close();
	      }
	}
	
}

class ChatServerThreadRunnable implements Runnable {
	
	protected ChatServerThread ChatServerThread = null;
	protected SSLSocket clientSocket = null;
	protected PrintWriter out = null;
	protected BufferedReader in = null;
	public int clientID = -1;
	
	public ChatServerThreadRunnable (ChatServerThread server, SSLSocket socket) {
		
		this.ChatServerThread = server;
		this.clientSocket = socket;
		clientID = clientSocket.getPort();
		
		try {
			
			out = new PrintWriter (clientSocket.getOutputStream(),true);
			in = new BufferedReader ( new InputStreamReader (clientSocket.getInputStream()));
			
		}
		catch (IOException i) {
			
		}
	}
	
	public void run() {
		
		try {
			
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				
				System.out.println(inputLine);
				ChatServerThread.putClient(getClientID(),getClientID() + ":" +inputLine);

				if (inputLine.equalsIgnoreCase("Bye."))
					break;
			}
		}
		catch (IOException e) {
			
			ChatServerThread.delClient(getClientID());
			
		}
	}

	public int getClientID() {
		
		return clientID;
	}
	
	public void close() {
		
		try {
			
			if (in != null) in.close();
			if (out != null) out.close();
			if (clientSocket != null) clientSocket.close();
			
		}
		catch (IOException i) {
			
		}
		
	}
	
}
