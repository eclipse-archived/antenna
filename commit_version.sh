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

git checkout $1
mvn clean install -DskipTests -Dqualifier=-$(git describe --tags --abbrev=0 | cut -d"-" -f4-)-$(git log $(git describe --tags --abbrev=0)..HEAD --oneline | wc -l)-$(git rev-parse --short HEAD)
