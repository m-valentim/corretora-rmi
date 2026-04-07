import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
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

    private String getIpCliente() {
        try {
            return RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            return "IP Desconhecido";
        }
    }

    private void salvarLog(String mensagem) {
        System.out.println(mensagem); 
        try (FileWriter fw = new FileWriter("log.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(mensagem); 
        } catch (IOException e) {
            System.out.println("Erro ao escrever no arquivo de log.");
        }
    }

    @Override
    public boolean cadastrarAcao(String nome, double preco, ClienteInterface emissor) throws RemoteException {
        // VALIDAÇÃO: Impede cadastro de ação com nome já existente
        if (acoes.containsKey(nome)) {
            return false; 
        }

        acoes.put(nome, preco);
        salvarLog("[IP: " + getIpCliente() + "] Cadastrou NOVA ação: " + nome + " -> R$ " + preco);
        
        for (ClienteInterface cliente : clientesRegistrados) {
            try {
                if (!cliente.equals(emissor)) { 
                    cliente.notificarNovaAcao(nome, preco);
                }
            } catch (RemoteException e) {
                clientesRegistrados.remove(cliente);
            }
        }
        return true; // Sucesso
    }

    @Override
    public double consultarPreco(String nome) throws RemoteException {
        salvarLog("[IP: " + getIpCliente() + "] Consultou preço da ação: " + nome);
        return acoes.getOrDefault(nome, -1.0);
    }

    @Override
    public Map<String, Double> listarAcoes() throws RemoteException {
        salvarLog("[IP: " + getIpCliente() + "] Listou todas as ações.");
        return acoes;
    }

    @Override
    public boolean atualizarPreco(String nome, double preco, ClienteInterface emissor) throws RemoteException {
        // VALIDAÇÃO: Só atualiza se a ação existir
        if (!acoes.containsKey(nome)) {
            return false;
        }

        acoes.put(nome, preco);
        salvarLog("[IP: " + getIpCliente() + "] Atualizou preço: " + nome + " -> R$ " + preco);
        
        for (ClienteInterface cliente : clientesRegistrados) {
            try {
                if (!cliente.equals(emissor)) {
                    cliente.notificarMudanca(nome, preco);
                }
            } catch (RemoteException e) {
                clientesRegistrados.remove(cliente);
            }
        }
        return true; // Sucesso
    }

    @Override
    public boolean removerAcao(String nome, ClienteInterface emissor) throws RemoteException {
        // VALIDAÇÃO: Só remove se a ação existir
        if (!acoes.containsKey(nome)) {
            return false;
        }

        acoes.remove(nome);
        salvarLog("[IP: " + getIpCliente() + "] Removeu a ação: " + nome);
        
        for (ClienteInterface cliente : clientesRegistrados) {
            try {
                if (!cliente.equals(emissor)) {
                    cliente.notificarRemocao(nome);
                }
            } catch (RemoteException e) {
                clientesRegistrados.remove(cliente);
            }
        }
        return true; // Sucesso
    }

    @Override
    public void registrarCliente(ClienteInterface cliente) throws RemoteException {
        clientesRegistrados.add(cliente);
        salvarLog("Novo cliente registrado para notificações. [IP: " + getIpCliente() + "]");
    }

    @Override
    public void desconectarCliente(ClienteInterface cliente) throws RemoteException {
        String ipDesconectado = getIpCliente();
        clientesRegistrados.remove(cliente); 
        salvarLog("[IP: " + ipDesconectado + "] Saiu do servidor.");
        
        for (ClienteInterface c : clientesRegistrados) {
            try {
                c.notificarDesconexao(ipDesconectado);
            } catch (RemoteException e) {
                clientesRegistrados.remove(c);
            }
        }
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