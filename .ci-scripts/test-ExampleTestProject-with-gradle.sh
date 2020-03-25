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

set -ex

cd "$(dirname "$0")/.."

tmpdir=$(mktemp -d)
trap 'rm -rf $tmpdir' EXIT
cp -r example-projects/example-project/ $tmpdir/example-project/
if [[ $M2_REPOSITORY ]]; then
    sed -i.bak "s%mavenLocal()%maven {url '${M2_REPOSITORY}'}%" $tmpdir/example-project/build.gradle
fi

pushd $tmpdir/example-project
./gradlew cleanAnalyze analyze
popd

java -jar core/frontend-stubs-testing/target/antenna-test-project-asserter.jar ExampleTestProject $tmpdir/example-project/build
