import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CorretoraServidor extends UnicastRemoteObject implements CorretoraInterface {
    
    private Map<String, Double> acoes = new ConcurrentHashMap<>();
    private List<ClienteInterface> clientesRegistrados = new CopyOnWriteArrayList<>();

    public CorretoraServidor() throws RemoteException {
        super();
        acoes.put("BTC", 350000.0);
        acoes.put("ETH", 18000.0);
        acoes.put("SOL", 800.0);
    }

    // Método auxiliar para pegar o IP de quem fez a chamada remota
    private String getIpCliente() {
        try {
            return RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            return "IP Desconhecido";
        }
    }

    @Override
    public void cadastrarAcao(String nome, double preco, ClienteInterface emissor) throws RemoteException {
        acoes.put(nome, preco);
        System.out.println("[IP: " + getIpCliente() + "] Cadastrou NOVA ação: " + nome + " -> R$ " + preco);
        
        // Notifica todos de que uma nova ação entrou na bolsa
        for (ClienteInterface cliente : clientesRegistrados) {
            try {
                if (!cliente.equals(emissor)) {
                    cliente.notificarNovaAcao(nome, preco);
                }
            } catch (RemoteException e) {
                clientesRegistrados.remove(cliente);
            }
        }
    }

    @Override
    public double consultarPreco(String nome) throws RemoteException {
        System.out.println("[IP: " + getIpCliente() + "] Consultou preço da ação: " + nome);
        return acoes.getOrDefault(nome, -1.0);
    }

    @Override
    public Map<String, Double> listarAcoes() throws RemoteException {
        System.out.println("[IP: " + getIpCliente() + "] Listou todas as ações.");
        return acoes;
    }

    @Override
    public void atualizarPreco(String nome, double preco, ClienteInterface emissor) throws RemoteException {
        if (acoes.containsKey(nome)) {
            acoes.put(nome, preco);
            System.out.println("[IP: " + getIpCliente() + "] Atualizou preço: " + nome + " -> R$ " + preco);
            
            // Notifica alteração de preço
            for (ClienteInterface cliente : clientesRegistrados) {
                try {
                    if (!cliente.equals(emissor)) {
                        cliente.notificarMudanca(nome, preco);
                    }
                } catch (RemoteException e) {
                    clientesRegistrados.remove(cliente);
                }
            }
        }
    }

    @Override
    public void registrarCliente(ClienteInterface cliente) throws RemoteException {
        clientesRegistrados.add(cliente);
        System.out.println("Novo cliente registrado para notificações. [IP: " + getIpCliente() + "]");
    }

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "192.168.137.1"); 
            CorretoraServidor servidor = new CorretoraServidor();
            Registry registry = LocateRegistry.createRegistry(1099); 
            registry.rebind("Corretora", servidor);
            System.out.println("Servidor da Corretora em execução no IP 192.168.137.1...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}