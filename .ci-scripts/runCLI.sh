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

jar="$(readlink -f "$(dirname $0)/../assembly/cli/target/antenna.jar")"
echo "use the JAR=[$jar]"

if [[ $# -eq 0 ]] ; then
    echo 'no argument was passed'
    exit 1
fi
dirToRunIn="$1"
if [[ ! -d "$dirToRunIn" ]]; then
    echo "$dirToRunIn does not exist"
    exit 1
fi

java="java"
if [[ $2 = "-debug" ]] ; then
    java="$java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
fi

set -x
cd $dirToRunIn
$java -jar $jar ./pom.xml
