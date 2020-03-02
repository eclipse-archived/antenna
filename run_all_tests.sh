#!/usr/bin/env bash
# Copyright (c) Bosch Software Innovations GmbH 2019.
# Copyright (c) Bosch.IO GmbH 2020.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#!/usr/bin/env bash
set -e
set -o pipefail
trap times EXIT
cd "$(dirname "${BASH_SOURCE[0]}")"

runWithScope() {
    local msg="$1"
    shift
    echo "################################################################################"
    echo "## $msg"
    echo "################################################################################"
    time \
        $@ |
        sed -e 's/^/['"$msg"'] /'
}

have() { type "$1" &> /dev/null; }

if [[ "$1" == "--custom-repository" ]]; then
    export M2_REPOSITORY="/tmp/run_all_tests.repository"
    mkdir -p $M2_REPOSITORY
    export MAVEN_OPTS="-Dmaven.repo.local=$M2_REPOSITORY"
fi

runWithScope "mvn install"  \
    mvn --batch-mode clean install

runWithScope "static code analysis" \
    .ci-scripts/test-run-all-static-code-analysis.sh

runWithScope "integration test maven" \
    .ci-scripts/test-ExampleTestProject-with-maven.sh

runWithScope "integration test cli" \
    .ci-scripts/test-ExampleTestProject-with-CLI.sh

runWithScope "integration test gradle" \
    .ci-scripts/test-ExampleTestProject-with-gradle.sh

if [[ WITH_P2 == "true" ]]; then
    runWithScope "integration test p2" \
        .ci-scripts/test-p2-end2end.sh
fi

if [[ -f modules/sw360/src/test/resources/postgres/sw360pgdb.sql ]]; then
    runWithScope "integration test sw360" \
        .ci-scripts/test-sw360-integration-test.sh
fi

runWithScope "documentation site test" \
    .ci-scripts/test-antenna-documentation-site-tests.sh

runWithScope "test for license headers" \
    .ci-scripts/test-for-licenseHeaders.sh

