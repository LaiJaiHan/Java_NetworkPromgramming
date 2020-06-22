import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface RmiChatApplication extends Remote{

    public void auto(String msg) throws RemoteException;
    public Vector<String> log() throws RemoteException;
    
    public String hello(String input) throws RemoteException;
    
}