# Sistema Distribuído de Relógio Mundial

## Resumo do Projeto

Implementação de comunicação entre processos via Sockets em Java, cobrindo três versões progressivas:

| Versão  | Protocolo | Modelo                                                 |
| ------- | --------- | ------------------------------------------------------ |
| Parte 1 | UDP       | Não orientado à conexão, com timeout de 5 s no cliente |
| Parte 2 | TCP       | Single-thread, atendimento sequencial                  |
| Parte 3 | TCP       | Multithread, atendimento concorrente                   |

O cliente envia um ID de região geográfica (ex: `America/Sao_Paulo`) e recebe a data/hora local daquela zona formatada via `java.time.ZonedDateTime`, ou uma mensagem de erro para regiões inválidas.

---

## Estrutura de Pastas

```
├── udp-clock/
│   └── src/main/java/com/soquetes/
│        ├── UDPServer.java
│        └── UDPClient.java
├── tcp-clock-simple/
│   └── src/main/java/com/tcpsoquete/
│       ├── TCPServer.java
│       └── TCPClient.java
└── tcp_clock_multithread/
    └── src/main/java/com/tcp_clock_multithread/
        ├── TCPMultiServer.java
        └── TCPMultiClient.java
```

---

## Instruções de Execução

> **Pré-requisito:** JDK 17 ou superior instalado e `javac`/`java` no PATH.

### Parte 1: UDP (porta 9000)

```bash
# Entrar dentro da pasta correta
cd udp-clock/src/main/java/com/soquetes

# Terminal 1: servidor
java -cp out UDPServer.java

# Terminal 2: cliente
java -cp out UDPClient.java
```

### Parte 2: TCP Single-Thread (porta 9001)

```bash
# Entrar dentro da pasta correta
cd tcp-clock-simple/src/main/java/com/tcpsoquete

# Terminal 1: servidor
java -cp out TCPServer.java

# Terminal 2: cliente
java -cp out TCPClient.java
```

### Parte 3: TCP Multithread (porta 9002)

```bash
# Entrar dentro da pasta correta
cd tcp_clock_multithread/src/main/java/com/tcp_clock_multithread

# Terminal 1: servidor
java -cp out TCPMultiServer.java

# Terminais 2, 3, 4... (abra quantos quiser simultaneamente)
java -cp out TCPMultiClient.java
```

> Para demonstrar a concorrência da Parte 3, abra vários terminais de cliente ao mesmo tempo e observe no console do servidor que cada conexão recebe uma Thread distinta (ex: `Thread-0`, `Thread-1`, `Thread-2`...).

---

## Análise Técnica: Single-Thread vs Multithread

### Versão 2: TCP Single-Thread

O servidor possui um único fluxo de execução. O método `accept()` retorna um socket de cliente, o servidor o atende **até o fim** e só então volta ao `accept()` para aceitar o próximo. Na prática, se o Cliente A demorar 3 segundos para ser processado, o Cliente B que já estabeleceu conexão TCP (o SO aceita a conexão no backlog) ficará aguardando esses 3 segundos inteiros antes de receber qualquer resposta. Com N clientes simultâneos, o tempo de espera cresce de forma linear: o N-ésimo cliente aguarda a soma dos tempos de todos os anteriores.

### Versão 3: TCP Multithread

Assim que o `accept()` retorna, o servidor cria uma `Thread` dedicada e a delega o atendimento daquele cliente, retornando **imediatamente** ao `accept()`. Os clientes passam a ser processados em paralelo, limitados apenas pelos núcleos disponíveis na CPU e pela política de escalonamento da JVM. O tempo de resposta de cada cliente torna-se independente dos demais, o N-ésimo cliente não espera pelo N-1 anterior.

### Comparação Direta

| Cenário                    | Single-Thread            | Multithread                                     |
| -------------------------- | ------------------------ | ----------------------------------------------- |
| 1 cliente                  | Idêntico                 | Idêntico                                        |
| 10 clientes simultâneos    | Espera acumulada         | Atendimento paralelo                            |
| Operação lenta no servidor | Bloqueia todos os demais | Isola o impacto em 1 thread                     |
| Risco de Race Condition    | Nenhum (1 thread)        | Possível se houver estado compartilhado mutável |

A Versão 3 resolve o gargalo de serialização da Versão 2 ao custo de maior uso de memória (cada Thread consome sua própria pilha) e da necessidade de sincronização caso recursos compartilhados sejam introduzidos posteriormente.
