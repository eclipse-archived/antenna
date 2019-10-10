#!/usr/bin/env bash -e
# Copyright (c) Bosch Software Innovations GmbH 2019.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0

BASEDIR=$(readlink -f "$0")
BASEDIR=$(dirname "$BASEDIR")

PROJECT_VERSION=$1
# build antenna
mvn -q clean install -DskipTests
mvn -q site -f antenna-documentation/pom.xml

git checkout gh-pages

cp -TRv antenna-documentation/target/documentation/ ${PROJECT_VERSION}/

git add ${PROJECT_VERSION}/
git commit -m "Update documentation for ${PROJECT_VERSION}" -s

