import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClienteInterface extends Remote {
    void notificarMudanca(String acao, double novoPreco) throws RemoteException;
    void notificarNovaAcao(String acao, double preco) throws RemoteException;
    void notificarRemocao(String acao) throws RemoteException;
    void notificarDesconexao(String ipCliente) throws RemoteException;
}