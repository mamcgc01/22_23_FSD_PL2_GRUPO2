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
        System.out.print("Mensagem privada de " + nomeUtilizador + ": " + mensagemPrivada);
        return mensagemPrivada;
    }

    @Override
    public String sendMessageSecure(String nomeUtilizador, String mensagemPrivadaSegura, String signature, String pubkey, String pos) throws RemoteException {
        // TODO Auto-generated method stub
        String tudo = "Mensagem segura de "+ nomeUtilizador + ": " + mensagemPrivadaSegura + signature + pubkey + pos;
        return tudo;
    }

}