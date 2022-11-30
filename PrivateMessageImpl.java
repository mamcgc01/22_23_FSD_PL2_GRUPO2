import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class PrivateMessageImpl extends java.rmi.server.UnicastRemoteObject implements PrivateMessageInterface {

    public PrivateMessageImpl() throws RemoteException {
        super();
    }

    @Override
    public String sendMessage(String nomeUtilizador, String mensagemPrivada) throws RemoteException {
        // TODO Auto-generated method stub
        System.out.print(nomeUtilizador + ": " + mensagemPrivada);
        return mensagemPrivada;
    }

}