import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class PrivateMessageImpl extends java.rmi.server.UnicastRemoteObject implements PrivateMessageInterface {

    ArrayList<String> mensagensPrivada;

    public PrivateMessageImpl() throws RemoteException {
        super();
        this.mensagensPrivada = new ArrayList<>();
    }

    @Override
    public String sendMessage(String nomeUtilizador, String mensagemPrivada) throws RemoteException {
        // TODO Auto-generated method stub
        return nomeUtilizador;
    }

}