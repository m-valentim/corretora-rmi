import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface CorretoraInterface extends Remote {
    void cadastrarAcao(String nome, double preco, ClienteInterface emissor) throws RemoteException;
    double consultarPreco(String nome) throws RemoteException;
    Map<String, Double> listarAcoes() throws RemoteException;
    void atualizarPreco(String nome, double preco, ClienteInterface emissor) throws RemoteException;
    void registrarCliente(ClienteInterface cliente) throws RemoteException;
}