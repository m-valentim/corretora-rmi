import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClienteInterface extends Remote {
    void notificarMudanca(String acao, double novoPreco) throws RemoteException;
    void notificarNovaAcao(String acao, double preco) throws RemoteException;
}