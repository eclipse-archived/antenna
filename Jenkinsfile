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
    stages {
        stage('build antenna without the assembly') {
            steps {
                container('maven') {
                    sh 'mvn —B package -P \'!build-assembly\''
                }
            }
        }
    }
}
