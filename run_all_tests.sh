#!/usr/bin/env bash
# Copyright (c) Bosch Software Innovations GmbH 2019.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#!/usr/bin/env bash
set -e
trap times EXIT
cd "$(dirname "${BASH_SOURCE[0]}")"

logHeader() {
    set +x
    echo "################################################################################"
    echo "## $@"
    echo "################################################################################"
    set -x
}

have() { type "$1" &> /dev/null; }

set -x

WITH_P2="false"
if [[ "$1" == "--p2" ]]; then
    WITH_P2="true"
    modules/p2/prepareDependenciesForP2.sh
elif [[ "$1" == "--no-p2" ]]; then
    modules/p2/cleanupDependenciesForP2.sh
fi

logHeader "mvn install"
mvn clean install

set +x

# logHeader "do static code analysis"
# mvn install -DskipTests pmd:pmd checkstyle:checkstyle-aggregate spotbugs:check

logHeader "test maven"
.ci-scripts/test-ExampleTestProject-with-maven.sh

logHeader "test cli"
.ci-scripts/test-ExampleTestProject-with-CLI.sh

logHeader "test gradle"
.ci-scripts/test-ExampleTestProject-with-gradle.sh

if [[ WITH_P2 == "true" ]]; then
    logHeader "test p2"
    .ci-scripts/test-p2-end2end.sh
fi

if [[ -f modules/sw360/src/test/resources/postgres/sw360pgdb.sql ]]; then
    logHeader "test sw360 integration"
    .ci-scripts/test-sw360-integration-test.sh
fi

logHeader "test site"
.ci-scripts/test-antenna-documentation-site-tests.sh

logHeader "test for license headers"
.ci-scripts/test-for-licenseHeaders.sh

