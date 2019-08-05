#!/usr/bin/env bash
# Copyright (c) Bosch Software Innovations GmbH 2019.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0

set -e

cd "$(dirname "$0")/.."

if [[ ! -f modules/sw360/src/test/resources/postgres/sw360pgdb.sql ]]; then
    echo "The postgres dump is necessary at 'modules/sw360/src/test/resources/postgres/sw360pgdb.sql'"
    exit 1
fi

set -x

cd modules/sw360
mvn clean verify -DskipTests -P integration-test
