import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

public class RmiChatApplicationImpl extends UnicastRemoteObject implements RmiChatApplication {

    protected RmiChatApplicationImpl() throws RemoteException {
        super();
    }

    private static final long serialVersionUID = 1L;
    private Vector<String> wordlog = new Vector<String>();

    @Override
    public void auto(String msg){
        wordlog.addElement(msg);
    }
    
    @Override
    public Vector<String> log(){
        return wordlog;
    }

    @Override
    public String hello (String input) {
        
        String[] str = input.split(" ");

        // str[1] = name,
        // str[2] = grade, 
        // str[3] = university, 
        // str[4] = major

        String hello = "my name is "+str[1]+", "+str[2]+" student, studying in"+
            str[3]+", major in "+str[4]+". nice to meet you:)";

        return hello;

    }
        
    
}