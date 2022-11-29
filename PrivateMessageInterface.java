import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrivateMessageInterface extends Remote {
    String sendMessage(String name, String message) throws RemoteException;
}
