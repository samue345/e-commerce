# JMeter load tests

The test plan sends all requests to the Nginx entry point at
`http://localhost:8080`. Nginx distributes requests between the two Java
application instances.

The plan contains two standard JMeter thread groups:

- `Readers`: `GET /clientes/{id}`;
- `Writers`: `POST /clientes` with a unique generated email.

## Pagination endpoints

The customer list supports cursor pagination by default:

```text
GET /clientes?limit=20
GET /clientes?cursor=73&limit=20
```

The response contains `items`, `nextCursor`, and `hasNext`.

Traditional page pagination is also available:

```text
GET /clientes?page=2&size=20
```

The response contains `items`, `page`, `size`, and `hasNext`.

The simple pagination does not execute `COUNT(*)`; it fetches one extra row to
calculate `hasNext`.

## Run one scenario

From WSL, with Docker services running:

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

## Run all three scenarios

```bash
bash load-tests/run-scenarios.sh
```

The defaults are 50,000 records and 60 seconds per scenario. They can be
overridden without editing the test plan:

```bash
DATASET_SIZE=20000 DURATION_SECONDS=120 bash load-tests/run-scenarios.sh
```

The generated `.jtl` files and HTML reports are ignored by Git.
