#!/usr/bin/env bash
# Copyright (c) Bosch Software Innovations GmbH 2019.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
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

runMvnInstall() {
    logHeader "mvn install"
    set -x
    mvn clean
    mvn install
}

runStaticCodeAnalysis() {
    logHeader "do static code analysis"
    set -x
    mvn install -DskipTests pmd:pmd
}

runCLITests() {
    logHeader "test cli"
    (
        set -ex
        .travis/runCLI.sh example-projects/example-project
        java -jar core/frontend-stubs-testing/target/antenna-test-project-asserter.jar ExampleTestProject example-projects/example-project/target
    )
}

runGradleTests() {
    have gradle && {
        logHeader "test gradle"
        (
            set -ex
            cd example-projects/example-project
            gradle analyze
            java -jar ../../core/frontend-stubs-testing/target/antenna-test-project-asserter.jar ExampleTestProject build
        )
    }
}

runSW360IntegrationTests() {
    (
        if [[ -f modules/sw360/src/test/resources/postgres/sw360pgdb.sql ]]; then
            set -ex
            cd modules/sw360
            mvn clean verify -DskipTests -P integration-tes
        else
            echo "no SQL dump for SW360 integration testing present"
        fi
    )
}

runP2IntegrationTests() {
    logHeader "test p2"
    if [[ -f modules/p2/p2-product/repository_manager/target/products ]]; then
        (
            set -ex
            .travis/p2e2etest.sh
        )
    else
        echo "p2 is not prepared, skip the integration tests"
    fi
}

runSiteTest() {
    logHeader "test site"
    (
        set -ex
        cd antenna-documentation
        mvn site -Psite-tests
    )
}

runLicenseHeaderTest() {
    logHeader "test for license headers"
    .travis/testForLicenseHeaders.sh
}

# modules/p2/prepareDependenciesForP2.sh
runMvnInstall
runStaticCodeAnalysis
runCLITests
runGradleTests
runSW360IntegrationTests
runP2IntegrationTests
runSiteTest
runLicenseHeaderTest

