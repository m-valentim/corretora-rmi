import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class CorretoraCliente extends UnicastRemoteObject implements ClienteInterface {

    public List<String> notificacoesPendentes = new CopyOnWriteArrayList<>();

    protected CorretoraCliente() throws RemoteException {
        super();
    }

    @Override
    public void notificarMudanca(String acao, double novoPreco) throws RemoteException {
        notificacoesPendentes.add("[ALERTA] O preço de " + acao + " foi atualizado para R$ " + novoPreco);
    }

    @Override
    public void notificarNovaAcao(String acao, double preco) throws RemoteException {
        notificacoesPendentes.add("[NOVIDADE] Nova ação cadastrada: " + acao + " custando R$ " + preco);
    }

    @Override
    public void notificarRemocao(String acao) throws RemoteException {
        notificacoesPendentes.add("[AVISO] A ação " + acao + " foi removida da bolsa.");
    }

    @Override
    public void notificarDesconexao(String ipCliente) throws RemoteException {
        notificacoesPendentes.add("[SISTEMA] O cliente do IP " + ipCliente + " saiu da corretora.");
    }

    // Função auxiliar para evitar repetição de código
    private static void mostrarAcoesNaTela(CorretoraInterface corretora) throws RemoteException {
        System.out.println("\n--- AÇÕES DISPONÍVEIS ---");
        Map<String, Double> acoes = corretora.listarAcoes();
        if (acoes.isEmpty()) {
            System.out.println("Nenhuma ação na bolsa no momento.");
        } else {
            for (Map.Entry<String, Double> acao : acoes.entrySet()) {
                System.out.println("- " + acao.getKey() + ": R$ " + acao.getValue());
            }
        }
        System.out.println("-------------------------");
    }

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "192.168.137.34");

            CorretoraCliente cliente = new CorretoraCliente();
            CorretoraInterface corretora = conectarServidor(cliente);
            
            Scanner scanner = new Scanner(System.in).useLocale(java.util.Locale.US);

            while (true) {
                try {
                    if (!cliente.notificacoesPendentes.isEmpty()) {
                        System.out.println("\n--- NOTIFICAÇÕES RECENTES ---");
                        for (String alerta : cliente.notificacoesPendentes) {
                            System.out.println(alerta);
                        }
                        cliente.notificacoesPendentes.clear(); 
                    }

                    System.out.println("\n--- MENU ---");
                    System.out.println("1- Listar  |  2- Consultar  |  3- Atualizar");
                    System.out.println("4- Cadastrar  |  5- Remover  |  6- Sair");
                    System.out.print("Escolha uma opção: ");
                    int opcao = scanner.nextInt();

                    if (opcao == 1) {
                        mostrarAcoesNaTela(corretora);
                    } else if (opcao == 2) {
                        System.out.print("Nome da ação (ex: BTC): ");
                        String nome = scanner.next();
                        double preco = corretora.consultarPreco(nome);
                        if (preco != -1.0) {
                            System.out.println("Preço: R$ " + preco);
                        } else {
                            System.out.println("Ação não encontrada.");
                        }
                    } else if (opcao == 3) {
                        mostrarAcoesNaTela(corretora); // Mostra as opções antes de pedir o nome
                        System.out.print("Nome da ação para atualizar: ");
                        String nome = scanner.next();
                        System.out.print("Novo preço: ");
                        double preco = scanner.nextDouble();
                        
                        boolean sucesso = corretora.atualizarPreco(nome, preco, cliente);
                        if (sucesso) {
                            System.out.println("[SUCESSO] Preço atualizado e investidores notificados!");
                        } else {
                            System.out.println("[FALHA] Ação não encontrada na bolsa.");
                        }

                    } else if (opcao == 4) {
                        System.out.print("Nome da NOVA ação: ");
                        String nome = scanner.next();
                        System.out.print("Preço inicial da ação: ");
                        double preco = scanner.nextDouble();
                        
                        boolean sucesso = corretora.cadastrarAcao(nome, preco, cliente);
                        if (sucesso) {
                            System.out.println("[SUCESSO] Ação cadastrada na bolsa!");
                        } else {
                            System.out.println("[FALHA] Já existe uma ação com esse nome cadastrada.");
                        }

                    } else if (opcao == 5) {
                        mostrarAcoesNaTela(corretora); // Mostra as opções antes de pedir o nome
                        System.out.print("Nome da ação para REMOVER: ");
                        String nome = scanner.next();
                        
                        boolean sucesso = corretora.removerAcao(nome, cliente);
                        if (sucesso) {
                            System.out.println("[SUCESSO] Ação removida da bolsa definitivamente!");
                        } else {
                            System.out.println("[FALHA] Ação não encontrada na bolsa.");
                        }

                    } else if (opcao == 6) {
                        System.out.println("Desconectando da corretora... Até logo!");
                        try {
                            corretora.desconectarCliente(cliente);
                        } catch (Exception ignored) {}
                        System.exit(0);
                    } else {
                        System.out.println("Opção inválida.");
                    }
                } catch (java.util.InputMismatchException ex) {
                    System.out.println("\n[ERRO] Digite uma opção ou preço válido. Use ponto para os centavos.");
                    scanner.nextLine(); 
                } catch (RemoteException e) {
                    System.out.println("\n[ERRO] Conexão com o servidor perdida!");
                    corretora = conectarServidor(cliente); 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CorretoraInterface conectarServidor(CorretoraCliente cliente) {
        CorretoraInterface corretora = null;
        while (corretora == null) {
            try {
                corretora = (CorretoraInterface) Naming.lookup("rmi://192.168.137.1/Corretora");
                corretora.registrarCliente(cliente); 
                System.out.println("Conectado ao Servidor com sucesso!");
            } catch (Exception e) {
                System.out.println("Aguardando servidor... tentando novamente em 3 segundos.");
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }
        return corretora;
    }
}