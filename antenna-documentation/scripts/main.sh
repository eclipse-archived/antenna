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

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

PROJECT_VERSION="$(mvn -q\
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)"

if [[ "$PROJECT_VERSION" == *"SNAPSHOT" ]]; then
  "${BASEDIR}"/execute_github_site_maven_plugin.sh SNAPSHOT
else
  "${BASEDIR}"/documentation_release.sh
fi
