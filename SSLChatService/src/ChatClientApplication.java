

public class ChatClientApplication {

    public static void main(String[] args){

        if (args.length != 2){

            System.out.println("Usage : Classname ServerName ServiceName");
            
            System.exit(1);
        }

        String mServer = args[0];
        String mServiceName = args[1];

        System.out.println("Remote Method Invocate to "+mServer+", Service name:"
        +mServiceName);

        
        try{
            new ChatClient(mServer,mServiceName).run();
        } 
        catch (Exception e){
            System.err.println(e);
        }

    }
}