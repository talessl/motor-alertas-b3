# Motor de Alertas B3

Sistema distribuído que coleta cotações de ações da B3, avalia regras de alerta cadastradas pelo usuário e envia a notificação por Telegram.

O projeto foi construído para praticar mensageria, persistência e arquitetura em camadas com o ecossistema Spring. Ele já resolve um caso de uso real: **"me avise quando PETR4 passar de R$ 40"**.

## Como funciona

```
MotorPrecosScheduler ──► [precos.fila] ──► PrecoConsumer ──► AvaliadorAlertaService
   (coleta cotações)                         (consome)         (atualiza preço e avalia regras)
                                                                        │
                                                                        ▼
                      Telegram ◄── NotificacaoService ◄── AlertaConsumer ◄── [alertas.fila]
```

1. O **scheduler** busca a cotação de cada ativo cadastrado a cada 60 segundos, consultando a API da [brapi.dev](https://brapi.dev), e publica uma mensagem em `precos.fila`.
2. O **consumer de preços** recebe a mensagem e chama o avaliador, que atualiza o último preço do ativo e consulta os alertas habilitados daquele ticker.
3. Quando a condição de um alerta é atendida (e o intervalo mínimo entre disparos já passou), o avaliador publica em `alertas.fila`.
4. O **consumer de alertas** envia a notificação ao Telegram.

Como as etapas se comunicam por filas, o coletor, o avaliador e o notificador são independentes: se o Telegram ficar fora do ar, as mensagens esperam na fila, e é possível aumentar o número de consumers para processar mais ativos em paralelo.

## Tecnologias

- Java 21
- Spring Boot (Web, Data JPA, AMQP)
- MySQL
- RabbitMQ
- Docker
- Lombok
- API de cotações [brapi.dev](https://brapi.dev)
- API de bots do Telegram

## Estrutura do projeto

Na raiz: `docker-compose.yml` (MySQL e RabbitMQ) e `pom.xml`. Código-fonte:

```
com.example.demo
├── config        # RabbitConfig (filas, exchange, bindings, conversor JSON)
├── controller    # Endpoints REST
├── dto           # Records de entrada da API
├── messaging     # Producers, consumers e mensagens (records)
├── model         # Entidades JPA (Ativo, Alerta) e enum Condicao
├── repository    # Repositórios Spring Data JPA
├── scheduler     # MotorPrecosScheduler
└── service       # Regras de negócio, avaliação de alertas e notificação
```

## Como rodar

### Pré-requisitos

- JDK 17 ou superior
- Docker
- Um token gratuito da [brapi.dev](https://brapi.dev) (crie uma conta e copie o token do painel)
- Um bot do Telegram (crie com o [@BotFather](https://t.me/BotFather) e anote o token)
- O seu `chat id` (envie uma mensagem ao bot e abra `https://api.telegram.org/bot<TOKEN>/getUpdates`)

### 1. Subir MySQL e RabbitMQ

Na raiz do projeto existe um `docker-compose.yml` com os dois serviços:

```bash
docker compose up -d
docker compose ps
```

Aguarde os dois aparecerem como `healthy`. Os dados ficam em volumes nomeados e sobrevivem a `docker compose down`; para apagar tudo, use `docker compose down -v`.

| Serviço | Porta | Acesso |
|---|---|---|
| MySQL 8 | 3306 | banco `b3_alerts`, usuário `root`, senha `root` |
| RabbitMQ | 5672 | `guest` / `guest` |
| Painel do RabbitMQ | 15672 | `http://localhost:15672` |

### 2. Configurar variáveis de ambiente

Segredos não ficam no código. Defina:

| Variável | Descrição |
|---|---|
| `BRAPI_TOKEN` | Token da API de cotações brapi.dev |
| `TELEGRAM_BOT_TOKEN` | Token do bot, fornecido pelo BotFather |
| `TELEGRAM_CHAT_ID` | Id do chat que receberá os alertas |

Configuração do banco e do RabbitMQ em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/b3_alerts
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

brapi.token=${BRAPI_TOKEN}

telegram.bot-token=${TELEGRAM_BOT_TOKEN}
telegram.chat-id=${TELEGRAM_CHAT_ID}
```

As tabelas são criadas automaticamente pelo Hibernate na primeira execução.

### 3. Executar a aplicação

```bash
export BRAPI_TOKEN=seu_token_brapi
export TELEGRAM_BOT_TOKEN=seu_token
export TELEGRAM_CHAT_ID=seu_chat_id
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd` e defina as variáveis com `set`. No IntelliJ, as variáveis ficam em **Run → Edit Configurations → Environment variables**.

## API

### Cadastrar um ativo

`POST /api/ativos`

```json
{
  "ticker": "PETR4",
  "ultimoPreco": 35.50
}
```

### Criar um alerta

`POST /api/alertas`

```json
{
  "ticker": "PETR4",
  "condicao": "ACIMA_DE",
  "valorAlvo": 40.00
}
```

Condições disponíveis: `ACIMA_DE` e `ABAIXO_DE`. O ativo precisa estar cadastrado antes.

### Listar alertas

`GET /api/alertas`

### Testar a notificação

`POST /api/alertas/teste` publica uma mensagem diretamente em `alertas.fila`, sem esperar o scheduler:

```json
{
  "ticker": "PETR4",
  "preco": 35.50,
  "descricao": "Preço acima do alvo"
}
```

## Exemplo de uso

1. Cadastre `PETR4` em `/api/ativos`.
2. Crie um alerta `ACIMA_DE` com valor alvo abaixo da cotação atual.
3. No próximo ciclo do scheduler (até 60 segundos), a mensagem chega no seu Telegram.

## Decisões de projeto

- **Consumers finos:** os `@RabbitListener` só recebem a mensagem e delegam para um service, o que isola a regra de negócio da tecnologia de mensageria.
- **`BigDecimal` nos valores de alerta:** evita imprecisão de ponto flutuante em dados monetários.
- **Intervalo mínimo entre disparos:** o campo `ultimoDisparo` impede que o mesmo alerta notifique a cada ciclo.
- **Retry limitado nos consumers:** uma falha no envio ao Telegram não deixa a mensagem em loop infinito.
- **Configuração externa:** os tokens da brapi e do Telegram e o chat id vêm de variáveis de ambiente.
- **Cliente HTTP resiliente:** a consulta à brapi tem timeout de conexão e leitura, e os logs de erro registram só o ticker e o status HTTP, para não expor o token.

## Limitações conhecidas e próximos passos

- Dead Letter Queue para inspecionar mensagens que falharam
- Concorrência configurável nos consumers para processar muitos ativos em paralelo
- Respeitar o limite de requisições do plano gratuito da brapi (consultar vários tickers por chamada)
- Mais tipos de alerta (variação percentual, volume, indicadores)
- Padrão *outbox* para garantir consistência entre o banco e a publicação na fila
- Testes automatizados dos services e da avaliação de regras

## Autor

**Tales Silva Leopoldino**
[GitHub](https://github.com/talessl) · [LinkedIn](https://linkedin.com/in/tales-leopoldino/) · [Portfólio](https://talessl.github.io)
