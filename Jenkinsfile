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
      mountPath: /root/.m2
    resources:
        requests:
            memory: "4096Mi"
        limits:
            memory: "4096Mi"
"""
        }
    }
    environment {
        MAVEN_OPTS = '-Xms4G -Xmx4G'
    }
    parameters {
        choice(
            choices: ['build' , 'build_and_deploy_snapshot'],
            description: '',
            name: 'REQUESTED_ACTION')
    }
    stages {
        stage('build antenna without the assembly') {
            steps {
                container('maven') {
                    sh 'mvn -B package -P \'!build-assembly\''
                }
            }
        }
        stage ('deploy snapshot') {
            when {
                expression { params.REQUESTED_ACTION == 'build_and_deploy_snapshot' }
            }
            steps {
                sh 'rm -rf repository'
                sh 'mkdir -p repository'
                container('maven') {
                    sh 'mvn -B package eclipse-jarsigner:sign deploy -P \'!build-assembly\' -DaltDeploymentRepository=snapshot-repo::default::file:$(readlink -f ./repository)'
                }
                sh 'ls repository/org/eclipse/sw360/antenna/'
                sh ' find repository -iname \'*.jar\' -print -exec jarsigner -verify {} \;'
                // sshagent ( ['project-storage.eclipse.org-bot-ssh']) {
                //     sh '''
                //       ssh genie.projectname@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/antenna/snapshots
                //       ssh genie.projectname@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/antenna/snapshots
                //       scp -r ./repository/* genie.projectname@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/antenna/snapshots
                //     '''
                // }
            }
        }
    }
}
