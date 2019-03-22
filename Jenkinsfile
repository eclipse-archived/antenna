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

pipeline {
    agent {
        kubernetes {
            label 'antenna-build-pod'
                        yaml """
apiVersion: v1
kind: Pod
spec:
  restartPolicy: Never
  volumes:
  - name: maven-p2
    emptyDir: {}
  containers:
  - name: maven
    image: maven:3.6.0-jdk-8-alpine
    command:
    - cat
    tty: true
    volumeMounts:
    - name: maven-p2
      mountPath: /home/jenkins/.m2
    resources:
        requests:
            memory: "2048Mi"
        limits:
            memory: "2048Mi"
"""
        }
    }
    environment {
        MAVEN_OPTS = '-Xms2G -Xmx2G'
    }
    parameters {
        choice(
            choices: ['build' , 'build_and_deploy_snapshot'],
            description: '',
            name: 'REQUESTED_ACTION')
        // string(
        //     defaultValue: 'HEAD',
        //     description: '',
        //     name: 'TAG_TO_BUILD')
    }
    stages {
        stage('build') {
            steps {
                container('maven') {
                    sh 'mvn -B install -DskipTests -P \'!build-assembly\''
                }
            }
        }
        stage('test') {
            steps {
                container('maven') {
                    sh 'mvn -B test -P \'!build-assembly\''
                }
            }
        }
        stage ('deploy snapshot') {
            when {
                expression { params.REQUESTED_ACTION == 'build_and_deploy_snapshot' }
            }
            stages {
                stage ('create local repository with signed jars') {
                    steps {
                        sh 'rm -rf repository'
                        sh 'mkdir -p repository'
                        container('maven') {
                            sh 'mvn -B package eclipse-jarsigner:sign deploy -DskipTests -P \'!build-assembly\' -pl \'!antenna-testing,!antenna-testing/antenna-core-common-testing,!antenna-testing/antenna-frontend-stubs-testing,!antenna-testing/antenna-rule-engine-testing\' -DaltDeploymentRepository=snapshot-repo::default::file:$(readlink -f ./repository)'
                        }
                    }
                }
                stage ('verify local repository') {
                    steps {
                        sh 'ls repository/org/eclipse/sw360/antenna/'
                        container('maven') {
                            sh 'find repository -iname \'*.jar\' -print -exec jarsigner -verify {} \\;'
                        }
                    }
                }
                // stage ('push local repository') {
                //     steps {
                //         sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
                //             // sh '''
                //             //   ssh genie.antenna@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/antenna/snapshots
                //             //   ssh genie.antenna@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/antenna/snapshots
                //             //   scp -r ./repository/* genie.antenna@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/antenna/snapshots
                //             // '''
                //         }
                //     }
                // }
            }
        }
    }
}
