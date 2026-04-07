import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class CorretoraCliente extends UnicastRemoteObject implements ClienteInterface {

    protected CorretoraCliente() throws RemoteException {
        super();
    }

    @Override
    public void notificarMudanca(String acao, double novoPreco) throws RemoteException {
        System.out.println("\n[ALERTA DA BOLSA] O preço de " + acao + " foi atualizado para R$ " + novoPreco);
        System.out.print("Escolha (1-Listar, 2-Consultar, 3-Atualizar, 4-Cadastrar): "); 
    }

    @Override
    public void notificarNovaAcao(String acao, double preco) throws RemoteException {
        System.out.println("\n[NOVIDADE NA BOLSA] Uma nova ação foi cadastrada: " + acao + " custando R$ " + preco);
        System.out.print("Escolha (1-Listar, 2-Consultar, 3-Atualizar, 4-Cadastrar): "); 
    }

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "192.168.137.34");

            CorretoraCliente cliente = new CorretoraCliente();
            CorretoraInterface corretora = conectarServidor(cliente);
            
            Scanner scanner = new Scanner(System.in).useLocale(java.util.Locale.US);

            while (true) {
                try {
                    System.out.println("\n--- MENU ---");
                    System.out.print("Escolha (1-Listar, 2-Consultar, 3-Atualizar, 4-Cadastrar): ");
                    int opcao = scanner.nextInt();

                    if (opcao == 1) {
                        System.out.println("Ações disponíveis: " + corretora.listarAcoes());
                    } else if (opcao == 2) {
                        System.out.print("Nome da ação (ex: BTC): ");
                        String nome = scanner.next();
                        System.out.println("Preço: R$ " + corretora.consultarPreco(nome));
                    } else if (opcao == 3) {
                        System.out.print("Nome da ação para atualizar: ");
                        String nome = scanner.next();
                        System.out.print("Novo preço: ");
                        double preco = scanner.nextDouble();
                        corretora.atualizarPreco(nome, preco, cliente);
                    } else if (opcao == 4) {
                        System.out.print("Nome da NOVA ação: ");
                        String nome = scanner.next();
                        System.out.print("Preço inicial da ação: ");
                        double preco = scanner.nextDouble();
                        corretora.cadastrarAcao(nome, preco, cliente);
                        System.out.println("Ação cadastrada na bolsa com sucesso!");
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