# SW360 Module

This module contains the `SW360Updater` and the `SW360Enricher` workflow steps together with a library which implements REST for SW360.

## Integration testing

For integration testing one can enable the profile `integration-test`, e.g. via `cd module/sw360; mvn install -Pintegration-test`, to run integration tests.
As a prerequisite one needs the file `src/integrationtest/resources/postgres/sw360pgdb.sql`, which contains a dump of the postgres state of a provisioned SW360 Liferay.
The dump can be done with the following one-liner:
```bash
$ pg_dump -h $HOST -p $PORT -U $USER sw360pgdb > src/integrationtest/resources/postgres/sw360pgdb.sql
```

The source code for the integration tests is placed at `src/integrationtest/java/` and might not be found by the IDE by default.
E.g. in IntelliJ IDEA one needs to mark the folder as `Test Sources Root` before it can handle the files.
