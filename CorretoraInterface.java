import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface CorretoraInterface extends Remote {
    boolean cadastrarAcao(String nome, double preco, ClienteInterface emissor) throws RemoteException;
    double consultarPreco(String nome) throws RemoteException;
    Map<String, Double> listarAcoes() throws RemoteException;
    boolean atualizarPreco(String nome, double preco, ClienteInterface emissor) throws RemoteException;
    boolean removerAcao(String nome, ClienteInterface emissor) throws RemoteException;
    
    void registrarCliente(ClienteInterface cliente) throws RemoteException;
    void desconectarCliente(ClienteInterface cliente) throws RemoteException;
}