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
mvn clean package
echo "done, testing whether download succeeded"

if [ ! -f "target/antenna/dependencies/some_bundle_0.0.1.201904011221.jar" ]
  then
    echo "Not all artifacts where downloaded correctly. Abort."
    exit 1
fi

echo "Download suceeded. Test passed"
exit 0
