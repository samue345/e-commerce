# Customer CRUD com Java, PostgreSQL, Nginx e Apache JMeter

Este projeto implementa uma API CRUD de clientes. Ele foi criado para estudar
o comportamento de uma aplicação web quando vários clientes fazem leituras e
escritas ao mesmo tempo.

## Especificação do trabalho e andamento

O primeiro trabalho consiste em implementar uma aplicação CRUD, envolvendo
clientes, servidor web e banco de dados, e avaliar o desempenho da aplicação
quanto a leituras e escritas.

### Requisitos da aplicação e dos experimentos

- ✅ Implementar uma aplicação CRUD envolvendo clientes, servidor web e banco
  de dados.
- ✅ Avaliar o desempenho da aplicação quanto a leituras e escritas.
- ✅ Executar o cenário **A**: 50% de leituras e 50% de escritas.
- ✅ Executar o cenário **B**: 75% de leituras e 25% de escritas.
- ✅ Executar o cenário **C**: 25% de leituras e 75% de escritas.
- ✅ Considerar diferentes clientes simultâneos durante a avaliação. Por
  exemplo, para 100 clientes no cenário A, usar 50 clientes para leitura e 50
  para escrita.
- ✅ Preparar um plano de teste com o Apache JMeter.
- ✅ Preparar grupos de usuários para operações de leitura e escrita.
- ✅ Popular o banco com pelo menos 50 mil registros. Neste projeto, os dados
  são criados localmente por um gerador reproduzível.
- ✅ Avaliar o que acontece quando o dataset é reduzido para 20 mil registros.
- ✅ Avaliar o que acontece com a adição de índices no banco de dados.
- ✅ Escolher um índice para o experimento.
- ✅ Verificar se o desempenho das consultas melhora com o índice.
- ✅ Avaliar como a escolha entre um banco SQL e um banco NoSQL impacta o
  sistema.

### Entrega

- ❌ Entregar o trabalho em PDF no Moodle até **30 de setembro de 2026**.
- ✅ Preparar a definição da aplicação, o projeto da arquitetura e o cenário
  dos experimentos da Parte 1.
- ✅ Realizar a avaliação da arquitetura por meio dos experimentos da Parte 2.
- ❌ Considerar a entrega atrasada até **5 de outubro de 2026**, com desconto
  de um ponto por dia de atraso, caso seja necessário.

## Status do trabalho

A aplicação e os experimentos foram concluídos. Os resultados estão no
relatório e resumidos em `relatorio/metricas.csv`; os planos de execução das
consultas estão em `relatorio/explain/`. Veja a seção 9 para o resumo.

### Checklist

- ✅ Definição de uma aplicação CRUD de clientes.
- ✅ API Java com operações de criação, leitura, atualização e remoção.
- ✅ Servidor web com Javalin.
- ✅ Banco de dados PostgreSQL.
- ✅ Duas instâncias Java para receber requisições.
- ✅ Nginx como proxy reverso e load balancer.
- ✅ Docker Compose para executar a arquitetura.
- ✅ Dataset local e reproduzível com 50 mil registros.
- ✅ Geração alternativa de dataset com 20 mil registros.
- ✅ Paginação por cursor sem executar `COUNT(*)`.
- ✅ Plano de teste inicial do Apache JMeter.
- ✅ Configuração dos cenários de leitura e escrita no plano JMeter.
- ✅ Executar o cenário A: 50% de leituras e 50% de escritas.
- ✅ Executar o cenário B: 75% de leituras e 25% de escritas.
- ✅ Executar o cenário C: 25% de leituras e 75% de escritas.
- ✅ Repetir os cenários usando 20 mil registros.
- ✅ Medir tempo de resposta, throughput e erros.
- ✅ Criar e avaliar índices adicionais.
- ✅ Comparar os resultados com e sem índice.
- ✅ Avaliar o impacto da escolha entre SQL e NoSQL.
- ✅ Organizar os resultados e conclusões no relatório (Claude Docs).
- ❌ Exportar o relatório para PDF e preencher a capa.

### Estado atual dos índices

O banco é criado somente com o índice da chave primária `id`. O índice
escolhido no experimento, `idx_customers_email`, é criado e removido pelos
scripts de experimento e não faz parte da migração. Veja a seção 9.

## Trabalho 2: tolerância a falhas com replicação

O Trabalho 2 consiste em implementar e avaliar uma solução que ofereça algum
nível de tolerância a falhas para o Trabalho 1 usando replicação.

### Especificação e andamento

- ✅ Replicar os servidores web da aplicação.
- ✅ Configurar dois nós Java: `app-1` e `app-2`.
- ✅ Manter os servidores Java stateless.
- ✅ Adicionar um PostgreSQL primary e um PostgreSQL replica.
- ✅ Configurar replicação física assíncrona por WAL.
- ✅ Criar um procedimento para promover a réplica manualmente.
- ✅ Adicionar retry para a conexão inicial com o banco de dados.
- ✅ Configurar retry do Nginx entre os servidores Java em falhas de upstream.
- ❌ Executar o experimento com falha de um servidor web.
- ❌ Executar o experimento com falha do banco primary.
- ❌ Medir o tempo de recuperação após cada falha.
- ❌ Medir possíveis erros e perda de transações durante o failover.
- ❌ Finalizar o PDF com a Parte 1 e a Parte 2.

### a) Falha de servidor web ou banco de dados

Se `app-1` falhar, o Nginx pode continuar enviando requisições para `app-2`.
Como os servidores Java não guardam estado local, os clientes não precisam
ser redistribuídos para uma sessão específica.

Se o PostgreSQL primary falhar, as aplicações perdem a conexão com o banco.
Nesse caso, a réplica deve ser promovida usando:

```bash
bash database/replication/promote_replica.sh
```

Depois da promoção, as aplicações são recriadas apontando para
`postgres-replica`. O procedimento atual é manual, o que deixa claro o tempo
de recuperação e o impacto da falha no experimento.

Depois que o serviço principal original estiver disponível para ser
reintegrado, execute:

```bash
bash database/replication/rejoin_primary.sh
```

Esse script recria o volume do antigo primary a partir do banco promovido e o
inicia novamente como réplica.

### b) Réplicas e protocolo de atualização

A arquitetura usa:

- duas instâncias Java;
- um PostgreSQL primary;
- uma réplica PostgreSQL.

As escritas são realizadas no primary. O PostgreSQL envia os registros de
WAL para a réplica, que os reaplica de forma assíncrona. Essa escolha reduz o
custo de cada escrita, mas pode fazer com que uma transação muito recente não
chegue à réplica antes de uma falha do primary.

Durante o failover, a réplica é promovida e as aplicações são reconectadas ao
novo banco principal. O antigo primary precisa ser reinicializado como réplica
antes de voltar à arquitetura, para evitar divergência de dados.

### Retries implementados

- O `DatabaseConfig` tenta estabelecer a conexão inicial várias vezes, com
  espera progressiva entre as tentativas.
- O Nginx tenta o outro servidor Java quando encontra erro de conexão, timeout
  ou respostas `502`, `503` e `504`.
- Não há retry automático de `POST` e `PUT`, pois repetir uma escrita depois de
  uma falha de resposta pode criar registros duplicados.

### c) Servidores stateful ou stateless

Os servidores Java são **stateless**:

- não armazenam sessões em memória que sejam necessárias para outro servidor;
- não armazenam dados de clientes no container;
- usam o PostgreSQL como fonte persistente dos dados.

Isso permite que o Nginx retire um nó com falha sem exigir migração de sessão.
O PostgreSQL, por outro lado, é **stateful**, pois mantém os dados persistentes
e precisa de replicação para que outro nó possa assumir a função do primary.

### d) Impacto do banco SQL

O PostgreSQL é um banco SQL e oferece transações, restrições e consistência
adequadas para o CRUD de clientes. A replicação física mantém uma cópia muito
próxima do banco primary, mas exige controle do estado do WAL e um procedimento
de failover.

A principal consequência para esta solução é que as escritas precisam ser
direcionadas ao primary. A réplica não deve receber escritas enquanto estiver
em modo standby. Em troca, ela pode assumir o serviço quando for promovida.

### Cenário de experimentos do Trabalho 2

Os experimentos devem comparar a aplicação normal com situações de falha:

1. executar a aplicação sem falhas e registrar a referência;
2. parar `app-1` durante as requisições e observar o Nginx;
3. parar `app-2` e repetir o teste;
4. parar o PostgreSQL primary;
5. promover a réplica;
6. reconectar as aplicações;
7. medir erros, tempo de recuperação, throughput e dados disponíveis.

### Entrega do Trabalho 2

- ❌ Entregar o PDF no Moodle até **26 de outubro de 2026**.
- ✅ Preparar o projeto da arquitetura e o cenário de experimentos da Parte 1.
- ❌ Avaliar a arquitetura por meio dos experimentos da Parte 2.
- ❌ Considerar a entrega atrasada até **30 de outubro de 2026**, com desconto
  de um ponto por dia de atraso, caso seja necessário.

## 1. O que o projeto faz

A aplicação permite:

- consultar a saúde da aplicação;
- buscar um cliente pelo ID;
- listar clientes usando paginação por cursor;
- cadastrar um cliente;
- atualizar um cliente;
- remover um cliente.

## 2. Arquitetura

O fluxo de uma requisição é:

```text
Cliente ou Apache JMeter
            |
            v
       Nginx :8080
        /          \
       v            v
   app-1:8080   app-2:8080
        \          /
         v        v
       PostgreSQL :5432
```

### Componentes

- **Java + Javalin**: servidor HTTP e regras da API.
- **PostgreSQL**: banco de dados relacional.
- **HikariCP**: pool de conexões com o banco.
- **Nginx**: proxy reverso e balanceador de carga.
- **Docker Compose**: sobe todos os serviços.
- **Python**: gera um dataset local e reproduzível.
- **Apache JMeter**: simula vários clientes e mede o desempenho.

As duas instâncias Java são stateless. 

## 3. Organização do projeto

- `src/main/java`: código da aplicação Java.
- `src/main/java/br/edu/crud/AppConfig.java`: instancia os objetos e configura
  as rotas da aplicação.
- `customer/controller`: recebe as requisições da API.
- `customer/service`: contém as regras de negócio.
- `customer/repository`: acesso ao PostgreSQL por JDBC.
- `customer/dto`: objetos usados nas requisições, paginação e respostas.
- `customer/model`: modelo `Customer`.
- `customer/mapper`: converte o modelo para o DTO de resposta.
- `database/migrations`: criação da tabela `customers`.
- `database/generate_dataset.py`: gera os arquivos CSV.
- `database/populate_database.sh`: carrega o CSV no PostgreSQL.
- `nginx/nginx.conf`: configuração do proxy reverso e do balanceamento.
- `load-tests/clientes-crud.jmx`: plano de teste do Apache JMeter.
- `load-tests/run-scenarios.sh`: executa os três cenários de carga.
- `load-tests/run-experiments.sh`: executa a bateria completa de experimentos
  (50k/20k, com e sem índice, cenários A, B e C).
- `relatorio/metricas.py`: resume os resultados do JMeter em
  `relatorio/metricas.csv`.
- `relatorio/explain.sh`: grava os planos de execução das consultas em
  `relatorio/explain/`.

## 4. Requisitos

Para subir a aplicação, instale:

- Docker;
- Docker Compose;
- Python 3;
- WSL, caso esteja usando Windows.

Para executar os testes de carga, instale também o Apache JMeter. Ele pode ser
baixado no site oficial do projeto Apache JMeter e executado localmente.

O JMeter não é instalado dentro dos containers. Ele funciona como um cliente
externo que envia requisições para `http://localhost:8080`.

## 5. Como subir o projeto

Execute os comandos a partir da raiz do projeto.

### 5.1 Subir os serviços

```bash
docker compose up -d --build
```

Esse comando inicia:

- um PostgreSQL;
- duas instâncias da API Java;
- um Nginx.

Confira o estado dos containers:

```bash
docker compose ps
```

O acesso público da aplicação é feito pelo Nginx:

```text
http://localhost:8080
```

### 5.2 Verificar a saúde da aplicação

```bash
curl -i http://localhost:8080/health
```

Resposta esperada:

```json
{"status":"UP"}
```

Se aparecer `502 Bad Gateway` logo após o `docker compose up`, aguarde alguns
segundos. Nesse momento o Nginx pode ter iniciado antes de as instâncias Java
terminarem de conectar ao PostgreSQL.

## 6. Criar e carregar o dataset

Com o PostgreSQL em execução, o script `database/populate_database.sh` gera o
CSV caso ele não exista e carrega os registros no banco.

```bash
bash database/populate_database.sh 50000
```

Para usar 20 mil registros:

```bash
bash database/populate_database.sh 20000
```

O script:

1. cria o CSV, caso ele ainda não exista;
2. espera o PostgreSQL ficar pronto;
3. limpa a tabela `customers`;
4. reinicia os IDs;
5. carrega o CSV usando o comando `COPY` do PostgreSQL;
6. informa quantos registros foram carregados.

Para confirmar a quantidade de registros:

```bash
docker exec crud-postgres psql \
  -U clientes_user \
  -d clientes_db \
  -c "SELECT COUNT(*) FROM customers;"
```

## 7. Endpoints da API

Todas as requisições devem ser feitas pelo Nginx em `localhost:8080`.

### Health check

```http
GET /health
```

Exemplo:

```bash
curl http://localhost:8080/health
```

Resposta:

```json
{"status":"UP"}
```

### Buscar um cliente

```http
GET /clientes/{id}
```

Exemplo:

```bash
curl http://localhost:8080/clientes/1
```

Resposta:

```json
{
  "id": 1,
  "name": "Mariana Moreira",
  "email": "cliente1@exemplo.test",
  "city": "Curitiba",
  "age": 61,
  "createdAt": "2026-09-27T16:23:00.975925Z"
}
```

### Listar clientes

```http
GET /clientes?limit=20
```

Exemplo:

```bash
curl "http://localhost:8080/clientes?limit=3"
```

Resposta:

```json
{
  "items": [
    {
      "id": 1,
      "name": "Mariana Moreira",
      "email": "cliente1@exemplo.test",
      "city": "Curitiba",
      "age": 61,
      "createdAt": "2026-09-27T16:23:00.975925Z"
    }
  ],
  "nextCursor": 1,
  "hasNext": true
}
```

Para buscar a próxima página, use o valor de `nextCursor`:

```bash
curl "http://localhost:8080/clientes?cursor=1&limit=3"
```

Também é possível filtrar por cidade:

```bash
curl "http://localhost:8080/clientes?city=Curitiba&limit=10"
```

E por e-mail (usado no experimento de índices):

```bash
curl "http://localhost:8080/clientes?email=cliente7%40exemplo.test&limit=1"
```

A paginação por cursor não executa `COUNT(*)`. A aplicação busca uma linha a
mais do que o limite solicitado. Essa linha extra serve para descobrir se
existe uma próxima página.

### Cadastrar um cliente

```http
POST /clientes
Content-Type: application/json
```

Exemplo:

```bash
curl -X POST http://localhost:8080/clientes \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Ana Silva",
    "email": "ana.silva@example.com",
    "city": "Sao Paulo",
    "age": 30
  }'
```

A resposta possui status `201 Created` e retorna o cliente criado.

### Atualizar um cliente

```http
PUT /clientes
Content-Type: application/json
```

O `id` é enviado no próprio DTO da requisição:

```bash
curl -X PUT http://localhost:8080/clientes \
  -H 'Content-Type: application/json' \
  -d '{
    "id": 1,
    "name": "Ana Silva Atualizada",
    "email": "ana.atualizada@example.com",
    "city": "Curitiba",
    "age": 31
  }'
```

### Remover um cliente

```http
DELETE /clientes/{id}
```

Exemplo:

```bash
curl -i -X DELETE http://localhost:8080/clientes/1
```

Em caso de sucesso, a API retorna `204 No Content`.

## 8. O que o Apache JMeter faz neste projeto

Neste projeto, o JMeter é usado somente como cliente externo da API. Ele não
fica dentro dos containers, não acessa o PostgreSQL diretamente e não altera a
arquitetura da aplicação. O caminho das requisições é:

```text
JMeter -> Nginx -> app-1 ou app-2 -> PostgreSQL
```

### Configuração existente no projeto

O arquivo `load-tests/clientes-crud.jmx` representa dois grupos de requisições:

- **Readers**: leem clientes. O tipo de leitura é escolhido pela propriedade
  `readMode`:
  - `id` (padrão): `GET /clientes/{id}`, busca pela chave primária;
  - `email`: `GET /clientes?email=clienteN@exemplo.test`, busca por e-mail,
    usada no experimento de índices;
- **Writers**: enviam requisições `POST /clientes` para inserir clientes.

Por padrão, esse arquivo está preparado para:

- 50 usuários virtuais leitores;
- 50 usuários virtuais escritores;
- 100 usuários virtuais no total;
- dataset de 50 mil registros;
- duração de 60 segundos;
- aumento gradual dos usuários durante 5 segundos.

Cada grupo repete sua operação durante o tempo configurado. Os dados enviados
pelas requisições de escrita usam e-mails únicos, evitando conflitos com os
registros já existentes.

O script `load-tests/run-scenarios.sh` apenas executa o mesmo plano três vezes,
alterando a quantidade de leitores e escritores para representar os cenários
do trabalho. Ele não cria índices, não altera o código Java e não gera os
resultados antes de ser executado.

### Cenários do trabalho

Os cenários são configurados pela quantidade de leitores e escritores:

| Cenário | Leitores | Escritores | Total |
|---|---:|---:|---:|
| A | 50 | 50 | 100 |
| B | 75 | 25 | 100 |
| C | 25 | 75 | 100 |

### Executar um cenário

Com Docker em execução e com o comando `jmeter` disponível:

```bash
jmeter -n \
  -t load-tests/clientes-crud.jmx \
  -Jreaders=50 \
  -Jwriters=50 \
  -JdatasetSize=50000 \
  -JdurationSeconds=60 \
  -l load-tests/results/scenario-a.jtl \
  -e \
  -o load-tests/results/scenario-a-report
```

Parâmetros usados neste projeto:

- `-n`: executa sem abrir a interface gráfica;
- `-t`: indica o arquivo do plano de teste;
- `-Jreaders`: define a quantidade de leitores;
- `-Jwriters`: define a quantidade de escritores;
- `-JdatasetSize`: informa o tamanho do dataset usado para gerar IDs;
- `-JdurationSeconds`: define a duração do teste;
- `-JreadMode`: tipo de leitura, `id` (padrão) ou `email`;
- `-l`: salva os resultados brutos no arquivo `.jtl`;
- `-e`: gera um relatório HTML;
- `-o`: define a pasta do relatório HTML.

### Executar os três cenários

```bash
bash load-tests/run-scenarios.sh
```

Para testar com 20 mil registros e dois minutos por cenário:

```bash
DATASET_SIZE=20000 DURATION_SECONDS=120 \
  bash load-tests/run-scenarios.sh
```

Os resultados ficam em `load-tests/results/`. Essa pasta é ignorada pelo Git
porque os relatórios são gerados a cada execução.

> **Atenção:** esse script não recarrega o banco entre os cenários. Como os
> escritores inserem centenas de milhares de registros por execução, o cenário
> B já começa com uma tabela bem maior que o A. Para resultados comparáveis,
> use o `run-experiments.sh` abaixo.

### Executar todos os experimentos do trabalho

```bash
bash load-tests/run-experiments.sh
```

Executa 18 testes de 60 segundos (cerca de 25 minutos):

| Dataset | Leitura | Índice extra | Cenários |
|---|---|---|---|
| 50k e 20k | por `id` | nenhum | A, B, C |
| 50k e 20k | por `email` | nenhum | A, B, C |
| 50k e 20k | por `email` | `idx_customers_email` | A, B, C |

Antes de **cada** teste o script recarrega o banco com o dataset original e
cria ou remove o índice. Antes do primeiro teste há um aquecimento de 20
segundos, descartado. Os resultados ficam em `load-tests/results/experiments/`:
um `.jtl` e um relatório HTML do JMeter por teste, além de `runs.csv` com a
quantidade de registros antes e depois de cada teste.

## 9. Índices e resultados dos experimentos

A chave primária `id` já possui índice automático. Ela é usada pela busca por
ID e pela paginação por cursor.

### Escolha do índice

O `relatorio/explain.sh` mede cada consulta da API com `EXPLAIN ANALYZE`, sem
índice extra e com cada candidato (50 mil registros):

| Consulta | Sem índice extra | Com o candidato | Banco usou? |
|---|---|---|---|
| `email = ?` | 3,68 ms (lê a tabela inteira) | 0,12 ms com `(email)` | Sim |
| `city = ? ORDER BY id LIMIT 21` | 0,08 ms (chave primária + filtro) | 0,10 ms com `(city)`; 0,09 ms com `(city, id)` | Não |

O índice em `city` não é usado: há só 10 cidades (baixa seletividade) e a
consulta encontra os 21 primeiros pela chave primária depois de ler cerca de
200 linhas. O escolhido foi o índice em `email`:

```sql
CREATE INDEX idx_customers_email ON customers (email);
```

### Resumo dos resultados (vazão total, req/s, 100 usuários)

| Configuração | A (50/50) | B (75/25) | C (25/75) |
|---|---:|---:|---:|
| 50k, leitura por ID | 4.050 | 4.358 | 3.380 |
| 20k, leitura por ID | 3.985 | 3.760 | 3.609 |
| 50k, leitura por e-mail sem índice | 1.175 | 703 | 1.472 |
| 20k, leitura por e-mail sem índice | 1.300 | 1.302 | 1.540 |
| 50k, leitura por e-mail com índice | 4.106 | 4.553 | 4.033 |
| 20k, leitura por e-mail com índice | 3.419 | 3.549 | 3.564 |

Nenhuma das 18 execuções teve erro. A análise completa está no relatório.

## 10. Limpar o ambiente

Para parar os containers:

```bash
docker compose down
```

Para remover também o volume do PostgreSQL e recriar o banco do zero:

```bash
docker compose down -v
docker compose up -d --build
```

O comando `down -v` apaga os dados armazenados no volume local do PostgreSQL.
Use-o somente quando essa limpeza for desejada.
