import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Vector;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.net.MalformedURLException;
import java.rmi.*;

public class ChatClient implements Runnable {
	
	private String server = "";
	private String serviceName = "";
	private int port = 3030;
	
	private SSLSocket chatSocket = null;
	private SSLSocketFactory chatSslSocketFactory;

	public ChatClient (String server, String serviceName) {

		this.server = server;
		this.serviceName = serviceName;
		
	}

	public void run() {

		try {

			//to set SSLSocket
			System.setProperty("javax.net.ssl.trustStore", "trustedcerts");
			System.setProperty("javax.net.ssl.trustStorePassword", "jwlee9112405");

			chatSslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			chatSocket = (SSLSocket) chatSslSocketFactory.createSocket(server, port);
			
			//Chipher suites setting
			chatSocket.setEnabledCipherSuites(
				chatSocket.getEnabledCipherSuites()
			);

			// check SSLSocket 
			printSocketInfo(chatSocket);

			chatSocket.startHandshake();

			new Thread(new ClientReceiver(chatSocket)).start();
			new Thread(new ClientSender(chatSocket,server,serviceName)).start();

			// clientID = chatSocket.getLocalPort();
		} catch (BindException b) {
			System.out.println("Can't bind on: "+port);
			System.exit(1);
		} catch (IOException i) {
			System.out.println(i);
			System.exit(1);
		}
		
	}

	private static void printSocketInfo(SSLSocket s) {
		System.out.println("Socket class: "+s.getClass());
		System.out.println("   Remote address = "
				+s.getInetAddress().toString());
		System.out.println("   Remote port = "+s.getPort());
		System.out.println("   Local socket address = "
				+s.getLocalSocketAddress().toString());
		System.out.println("   Local address = "
				+s.getLocalAddress().toString());
		System.out.println("   Local port = "+s.getLocalPort());
		System.out.println("   Need client authentication = "
				+s.getNeedClientAuth());
		SSLSession ss = s.getSession();
		System.out.println("   Cipher suite = "+ss.getCipherSuite());
		System.out.println("   Protocol = "+ss.getProtocol());
	}
}



class ClientSender implements Runnable {
	
	private SSLSocket chatSocket = null;
	private RmiChatApplication rmiApp = null;
	
	ClientSender(SSLSocket socket, String server, String serviceName){
		
		this.chatSocket = socket;

		try {
            rmiApp = (RmiChatApplication)Naming.lookup("rmi://"+server+"/"+serviceName);
        } catch (MalformedURLException mue){
            System.out.println("MalformedUrlException:"+mue);
        } catch (RemoteException re){
            System.out.println(re);
        } catch (NotBoundException nbe){
            System.out.println(nbe);
        } catch (java.lang.ArithmeticException exception){
            System.out.println(exception);
        }
	}
	
	public void run() {
		
		Scanner KeyIn = null;
		PrintWriter out = null;
		
		try {

			KeyIn = new Scanner(System.in);
			out = new PrintWriter (chatSocket.getOutputStream(), true);
			
			String userInput ="";
			
			System.out.print( "Your are "+chatSocket.getLocalPort() + 
					", Type Message (\"Bye.\" to leave)\n");
			
			while ((userInput = KeyIn.nextLine()) != null) {
				
				if (userInput.equalsIgnoreCase("rmi/log")){

					System.out.println("======log======");
					Vector<String> wordlog = rmiApp.log();
					for (String word: wordlog){
						System.out.println(word);
					}
					System.out.println("===============");
					out.println("[system] printed chat log.");
					out.flush();
				}

				//usage : rmi/hello <name> <grade> <university> <department>
				if (userInput.contains("rmi/hello")){

					out.println(rmiApp.hello(userInput));
					out.flush();					

				}
				
				else if (userInput.equalsIgnoreCase("rmi/exit")) break;

				else {
					
					out.println(userInput);
					out.flush();
				}
				
			}
			
			KeyIn.close();
			out.close();
			chatSocket.close();
			
			} catch (IOException i) {
				try {
					if (out != null) out.close();
					if (KeyIn != null) KeyIn.close();
					if (chatSocket != null) chatSocket.close();
				} catch (IOException e) { }
				System.exit(1);
		
			}
	
	}

}


class ClientReceiver implements Runnable {
	
	private Socket chatSocket = null;
	
	ClientReceiver(Socket socket){

		this.chatSocket = socket;

	}
	
	public void run() {
		
		while (chatSocket.isConnected()) {
			
			BufferedReader in = null;
			
			try {
				in = new BufferedReader (
						new InputStreamReader(chatSocket.getInputStream()));
				String readSome = null;
				
				while ((readSome = in.readLine()) != null) {
					System.out.println(readSome);
				}
			} catch (IOException i) {
				
				try {
					if (in != null) in.close();
					if (chatSocket != null) chatSocket.close();
				} catch (IOException e) {
				}
				System.out.println("Leave.");
				System.exit(1);
			}
				
		}
	}

}
	
	
	
	



