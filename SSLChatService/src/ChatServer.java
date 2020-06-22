import java.rmi.*;

public class ChatServer {

    public ChatServer (String server, String servicename){

        try {

            RmiChatApplication c = new RmiChatApplicationImpl();
            Naming.rebind("rmi://"+server+":1099/"+servicename, c);
            
            //3030 fixed. 
            new ChatServerThread(3030,server,servicename).run();

        
        } catch (Exception e){
            System.err.println(e);
        }

    }


    public static void main(String[] args){

        if(args.length !=2){

            System.out.println("Usage : classname servername servicename");
            System.exit(1);
        
        }

        String mServer = args[0];
        String mServiceName = args[1];

        new ChatServer(mServer,mServiceName);

    }
}