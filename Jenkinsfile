/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/*
 * Standard Jenkinsfile to be easily applicable in
 * a local Jenkins infrastructure
 */

pipeline {
    agent any

    parameters {
        booleanParam(
            name: 'RUN_TESTS',
            defaultValue: true,
            description: '')
    }
    stages {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // build antenna
        stage('build') {
            steps {
                withMaven() {
                    // build antenna
                    sh """
                      mvn -Dmaven.repo.local=\$(readlink -f localRepository) \
                          --batch-mode \
                          install -DskipTests
                    """
                }
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // run tests and try to execute antenna
        stage('test') {
            when {
                environment name: 'RUN_TESTS', value: 'true'
            }
            steps {
                withMaven() {
                    // run maven tests
                    sh '''
                      mvn -Dmaven.repo.local=$(readlink -f localRepository) \
                        --batch-mode \
                        test
                    '''
                    // test as maven plugin
                    sh 'MAVEN_OPTS="-Dmaven.repo.local=$(readlink -f ./repository)" .ci-scripts/test-ExampleTestProject-with-maven.sh'
                    // run SW360 integration tests, if corresponding sqldump is present
                    sh '''
                      if [[ -f modules/sw360/src/test/resources/postgres/sw360pgdb.sql ]]; then
                          .ci-scripts/test-sw360-integration-test.sh
                      fi
                    '''
                    // test as CLI tool
                    sh '.ci-scripts/test-ExampleTestProject-with-CLI.sh'
                    // test as gradle plugin
                    sh 'M2_REPOSITORY="$(readlink -f ./repository)" .ci-scripts/test-ExampleTestProject-with-gradle.sh'
                    // test the antenna site
                    sh 'MAVEN_OPTS="-Dmaven.repo.local=$(readlink -f ./repository)" .ci-scripts/test-antenna-documentation-site-tests.sh'
                    // run static code analysis
                    sh '''
                      mvn install -DskipTests pmd:pmd checkstyle:checkstyle-aggregate spotbugs:check -Dmaven.repo.local=$(readlink -f localRepository)
                    '''
                }
            }
        }
    }
}
