# Customer CRUD com Java, PostgreSQL, Nginx e Apache JMeter

Este projeto implementa uma API CRUD de clientes. Ele foi criado para estudar
o comportamento de uma aplicação web quando vários clientes fazem leituras e
escritas ao mesmo tempo.

## Especificação do trabalho

O primeiro trabalho consiste em implementar uma aplicação CRUD, envolvendo
clientes, servidor web e banco de dados, e avaliar o desempenho da aplicação
quanto a leituras e escritas nos seguintes cenários:

- **A)** 50% de leituras e 50% de escritas;
- **B)** 75% de leituras e 25% de escritas;
- **C)** 25% de leituras e 75% de escritas.

A avaliação deve considerar que o sistema está sendo usado por diferentes
clientes. Pode ser usada uma ferramenta de teste de carga, como o Apache
JMeter. Por exemplo, para 100 clientes simultâneos, o cenário A deve usar 50
clientes fazendo leituras e 50 clientes fazendo escritas.

O banco deve ser populado com um dataset de pelo menos 50 mil registros,
disponível na internet, ou com um dataset criado por um gerador de dados.
Também devem ser analisados os seguintes pontos:

- o que acontece com o desempenho quando o dataset é reduzido para 20 mil
  registros;
- o que acontece quando são adicionados índices no banco de dados;
- qual índice deve ser escolhido;
- se o desempenho das consultas melhora com o índice;
- como a escolha entre um banco SQL e um banco NoSQL impacta o sistema.

### Entrega

- Entrega no Moodle em PDF: **30 de setembro de 2026**;
- Parte 1: definição da aplicação, projeto da arquitetura e cenário dos
  experimentos;
- Parte 2: avaliação da arquitetura por meio dos experimentos;
- Entrega atrasada permitida até **5 de outubro de 2026**, com desconto de um
  ponto por dia de atraso.

Esta especificação foi incluída no README para separar claramente o que já foi
implementado daquilo que ainda precisa ser medido no experimento.

## Status do trabalho

O código da aplicação e a infraestrutura foram implementados, mas a avaliação
de desempenho ainda não foi realizada. Os resultados dos cenários A, B e C
devem ser obtidos posteriormente pelas pessoas responsáveis pelos experimentos.

### Checklist

- [x] Definição de uma aplicação CRUD de clientes.
- [x] API Java com operações de criação, leitura, atualização e remoção.
- [x] Servidor web com Javalin.
- [x] Banco de dados PostgreSQL.
- [x] Duas instâncias Java para receber requisições.
- [x] Nginx como proxy reverso e load balancer.
- [x] Docker Compose para executar a arquitetura.
- [x] Dataset local e reproduzível com 50 mil registros.
- [x] Geração alternativa de dataset com 20 mil registros.
- [x] Paginação por cursor sem executar `COUNT(*)`.
- [x] Plano de teste inicial do Apache JMeter.
- [x] Configuração dos cenários de leitura e escrita no plano JMeter.
- [ ] Executar o cenário A: 50% de leituras e 50% de escritas.
- [ ] Executar o cenário B: 75% de leituras e 25% de escritas.
- [ ] Executar o cenário C: 25% de leituras e 75% de escritas.
- [ ] Repetir os cenários usando 20 mil registros.
- [ ] Medir tempo de resposta, throughput e erros.
- [ ] Criar e avaliar índices adicionais.
- [ ] Comparar os resultados com e sem índice.
- [ ] Avaliar o impacto da escolha entre SQL e NoSQL.
- [ ] Organizar os resultados e conclusões no PDF final.

### Estado atual dos índices

Nenhum índice adicional foi criado para antecipar os experimentos. A tabela
possui somente o índice automático criado pelo PostgreSQL para a chave
primária `id`.

O índice em `city` apresentado mais adiante neste documento é apenas uma
possibilidade para o experimento. Ele ainda não foi criado e deve ser avaliado
pelas pessoas responsáveis pela etapa de desempenho.

## 1. O que o projeto faz

A aplicação permite:

- consultar a saúde da aplicação;
- buscar um cliente pelo ID;
- listar clientes usando paginação por cursor;
- cadastrar um cliente;
- atualizar um cliente;
- remover um cliente.

Os nomes de classes, métodos, tabelas e colunas do código estão em inglês.
As explicações deste documento estão em português.

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

As duas instâncias Java são stateless. Isso significa que elas não guardam
dados localmente: as duas usam o mesmo PostgreSQL.

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

O dataset não depende de uma fonte externa. Ele é criado localmente pelo
script Python e contém somente as colunas necessárias para este trabalho:

- `name`;
- `email`;
- `city`;
- `age`.

O gerador usa uma semente fixa. Portanto, executar o script novamente com os
mesmos parâmetros produz a mesma massa de dados.

### 6.1 Gerar 50 mil registros

```bash
python3 database/generate_dataset.py --rows 50000
```

O arquivo será criado em:

```text
database/data/clientes_50000.csv
```

### 6.2 Gerar 20 mil registros

```bash
python3 database/generate_dataset.py --rows 20000
```

### 6.3 Popular o banco

Com o PostgreSQL em execução:

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

## 8. Como o Apache JMeter funciona neste projeto

O Apache JMeter é uma ferramenta para teste de carga. Ele cria usuários
virtuais, envia requisições para a aplicação e registra informações como:

- quantidade de requisições;
- tempo de resposta;
- requisições por segundo;
- quantidade de erros;
- percentis, como o p95.

Neste projeto, o JMeter está na pasta `load-tests` para que os experimentos
sejam reproduzíveis. Ele não substitui a aplicação e não acessa o PostgreSQL
diretamente. O caminho testado é:

```text
JMeter -> Nginx -> app-1 ou app-2 -> PostgreSQL
```

### O que existe no plano JMeter

O arquivo `load-tests/clientes-crud.jmx` possui dois grupos de usuários:

- **Readers**: fazem `GET /clientes/{id}`;
- **Writers**: fazem `POST /clientes`.

Por padrão, o plano usa:

- 50 usuários virtuais leitores;
- 50 usuários virtuais escritores;
- 100 usuários virtuais no total;
- dataset de 50 mil registros;
- duração de 60 segundos;
- aumento gradual dos usuários durante 5 segundos.

Cada usuário virtual executa repetidamente a operação do seu grupo durante o
tempo configurado. Os e-mails enviados pelos escritores são gerados de forma
única para evitar conflito com os dados existentes.

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

Explicação das opções principais:

- `-n`: executa sem abrir a interface gráfica;
- `-t`: indica o arquivo do plano de teste;
- `-Jreaders`: define a quantidade de leitores;
- `-Jwriters`: define a quantidade de escritores;
- `-JdatasetSize`: informa o tamanho do dataset usado para gerar IDs;
- `-JdurationSeconds`: define a duração do teste;
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

## 9. Índices e comparação dos experimentos

A chave primária `id` já possui índice automático. Ela é usada pela busca por
ID e pela paginação por cursor.

Para avaliar uma consulta filtrada por cidade, pode-se criar um índice
adicional:

```sql
CREATE INDEX idx_customers_city ON customers (city);
```

A comparação recomendada para o trabalho é:

1. executar o teste sem o índice adicional;
2. criar o índice em `city`;
3. executar o mesmo teste novamente;
4. comparar tempo de resposta, throughput e erros.

O resultado depende do tipo de consulta, da quantidade de registros e da
distribuição das cidades. O índice tende a ajudar consultas que filtram por
`city`, mas também ocupa espaço e pode aumentar um pouco o custo de inserts e
updates.

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
