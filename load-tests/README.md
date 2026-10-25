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

Cursor pagination does not execute `COUNT(*)`; it fetches one extra row to
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

## Fault-tolerance plans

The file `fault-tolerance.jmx` is a read-only plan used while a failure is
provoked externally. It continuously executes `GET /clientes/{id}` through the
Nginx endpoint. Read-only requests are used because they can be repeated during
recovery without creating duplicate customers.

### Failure of a Java node

Run the automated scenario:

```bash
bash load-tests/run-web-failure-test.sh
```

The script starts JMeter, stops `app-1` after 20 seconds, waits 30 seconds,
starts it again and waits for the test to finish. To test `app-2`:

```bash
APP_SERVICE=app-2 bash load-tests/run-web-failure-test.sh
```

### Database failover

Run the database scenario:

```bash
bash load-tests/run-database-failover-test.sh
```

The script starts JMeter, stops `postgres-primary`, promotes the replica and
keeps the read load running during the transition. After the experiment,
reintegrate the old primary:

```bash
bash database/replication/rejoin_primary.sh
```

The reports are saved under `load-tests/results/fault-tolerance/`. These plans
measure the effect of a failure; Docker and the shell scripts are responsible
for injecting the failure and performing the recovery.
