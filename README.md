# Sistema de Corretora Distribuída

Este projeto implementa um sistema distribuído em Java utilizando RMI (Remote Method Invocation) para simular o funcionamento de uma corretora de valores. Múltiplos clientes podem se conectar simultaneamente ao servidor central para consultar e operar ações na bolsa em tempo real.

## Funcionalidades

- **Listagem e Consulta:** Visualização de todas as ações disponíveis e busca pelo preço de uma ação específica.
- **Operações na Bolsa:** Cadastro de novas ações, atualização de preços e remoção de ações existentes.
- **Notificações em Tempo Real (Callbacks):** Os clientes são notificados sobre alterações na bolsa (mudanças de preço, novas ações, remoções ou saída de usuários). A regra de negócio garante que o autor da modificação não receba a própria notificação.
- **Buffer de Mensagens:** O cliente armazena notificações em segundo plano para não interromper a navegação do usuário no menu, exibindo os alertas apenas na tela inicial.
- **Tolerância a Falhas Básica:** O cliente possui um mecanismo de reconexão automática em loop caso a comunicação com o servidor seja perdida (ex: queda do servidor).
- **Suporte a Concorrência:** O servidor utiliza `ConcurrentHashMap` e `CopyOnWriteArrayList` para lidar com múltiplos acessos simultâneos.
- **Geração de Logs:** Todas as transações realizadas no servidor, juntamente com o IP do cliente responsável pela requisição, são registradas no console da máquina servidora e salvas no arquivo `log.txt`.

## Estrutura de Arquivos



- `CorretoraInterface.java`: Define o contrato de serviços oferecidos pelo servidor (Corretora).
- `ClienteInterface.java`: Define o contrato de callback, permitindo que o servidor envie mensagens ativas para os clientes.
- `CorretoraServidor.java`: Implementação do servidor, responsável por gerenciar o estado das ações, tratar requisições concorrentes, armazenar clientes ativos e gravar os logs.
- `CorretoraCliente.java`: Implementação do cliente, interface interativa via terminal e lógica de tratamento de exceções de entrada e rede.

## Configuração de Rede (Atenção)

Para que a comunicação RMI funcione entre máquinas físicas diferentes, os endereços IP devem ser configurados nos códigos fonte **antes da compilação**.

1. No arquivo `CorretoraServidor.java`, defina o IP da máquina que será o servidor:
```java
System.setProperty("java.rmi.server.hostname", "XXX.XXX.XXX.XXX"); 
```

2. No arquivo CorretoraCliente.java, defina o IP da máquina do cliente (necessário para receber o callback):

```java
System.setProperty("java.rmi.server.hostname", "XXX.XXX.XXX.XXY");
```
3. Ainda no arquivo CorretoraCliente.java, no método conectarServidor, aponte a URL para o IP do servidor:

```java
corretora = (CorretoraInterface) Naming.lookup("rmi://XXX.XXX.XXX.XXX/Corretora");
```

Nota: É necessário garantir que as máquinas conseguem se comunicar na **mesma rede** (via comando ping) e que o firewall não está bloqueando conexões locais.

## Como Executar

1. Compilação

Abra o terminal na pasta raiz onde os arquivos estão localizados e compile o código (faça isso em ambas as máquinas):

```bash
javac *.java
```

2. Iniciar o Servidor

Na máquina designada como servidor, inicie a aplicação. Ela criará automaticamente o registro RMI na porta padrão (1099):

```bash
java CorretoraServidor
```

3. Iniciar o Cliente

Na máquina designada como cliente, inicie a aplicação cliente:

```bash
java CorretoraCliente
```