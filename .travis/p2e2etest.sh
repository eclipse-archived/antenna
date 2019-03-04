#!/usr/bin/env bash
# Copyright (c) Bosch Software Innovations GmbH 2018.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0

set -e

echo "starting p2 e2e test"
folder="$(readlink -f "$(dirname $0)/../example-projects/p2-example-tycho-project/")"
echo "working directory [$folder]"

cd "$folder/"
cd dependency_project
mvn clean package
cd ..
mvn clean package
echo "done, testing whether download succeeded"

if [ ! -f "target/antenna/dependencies/org.eclipse.equinox.launcher_1.3.201.v20161025-1711.jar" ]
  then
    echo "Not all artifacts where downloaded correctly. Abort."
    exit 1
fi

if [ ! -f "target/antenna/dependencies/org.eclipse.equinox.launcher.source_1.3.201.v20161025-1711.jar" ]
  then
    echo "Not all artifacts where downloaded correctly. Abort."
    exit 1
fi

echo "Download suceeded. Test passed"
exit 0
