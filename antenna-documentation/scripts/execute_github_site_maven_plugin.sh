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

mvn -q -B clean install -DskipTests -P ci

GITHUB_USERNAME="${GITHUB_CREDENTIALS_USR}" \
    GITHUB_PASSWORD="${GITHUB_CREDENTIALS_PSW}" \
    ANTENNA_DOCUMENTATION_VERSION=$1 \
    mvn -f ./antenna-documentation/pom.xml \
        site-deploy -Psite-deploy
