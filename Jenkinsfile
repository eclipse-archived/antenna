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
            name: 'BUILD_WITH_ANTENNA_P2',
            defaultValue: true,
            description: '')
        booleanParam(
            name: 'RUN_TESTS',
            defaultValue: true,
            description: '')
    }
    stages {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // handle the p2 dependency part (the stuff below ./antena-p2)
        // this either gets created (if flag is true) or removed
        stage('build deps for p2') {
            when {
                environment name: 'BUILD_WITH_ANTENNA_P2', value: 'true'
            }
            steps {
                withMaven() {
                    sh "./modules/p2/prepareDependenciesForP2.sh"
                }
            }
        }
        stage('cleanup deps for p2') {
            when {
                environment name: 'BUILD_WITH_ANTENNA_P2', value: 'false'
            }
            steps {
                withMaven() {
                    sh "./modules/p2/cleanupDependenciesForP2.sh"
                }
            }
        }

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
                    sh '''
                      tmpdir="$(mktemp -d)"
                      locRep="$(readlink -f localRepository)"
                      cp -r example-projects/example-project $tmpdir
                      cd $tmpdir/example-project
                      mvn -Dmaven.repo.local="$locRep" \
                        --batch-mode \
                        package
                      cd -
                      rm -r "$tmpdir"
                    '''

                    // run SW360 integration tests, if corresponding sqldump is present
                    sh '''
                      if [[ -f modules/sw360/src/test/resources/postgres/sw360pgdb.sql ]]; then
                          cd modules/sw360
                          mvn clean verify -DskipTests -P integration-test
                          cd -
                      fi
                    '''

/* Diable CLI test on Jenkins for now, since it is not working
                    // test as CLI tool
                    sh '''
                      tmpdir="$(mktemp -d)"
                      cp -r example-projects/example-project $tmpdir
                      .travis/sh $tmpdir/example-project
                      java -jar antenna-testing/antenna-frontend-stubs-testing/target/antenna-test-project-asserter.jar ExampleTestProject $tmpdir/example-project/target
                      rm -r "$tmpdir"
                    '''
*/
                    // test the antenna site
                    dir("antenna-documentation") {
                        sh '''
                          mvn -Dmaven.repo.local=$(readlink -f ../localRepository) \
                            --batch-mode \
                            site -Psite-tests
                        '''
                    }

                    // run static code analysis
                    sh '''
                      mvn pmd:pmd
                    '''
                }
            }
        }
    }
}
