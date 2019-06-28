#!/usr/bin/env bash
# Copyright (c) Bosch Software Innovations GmbH 2019.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0

set -ex

cd "$(dirname $0)"
rm -r p2-product/repository_manager/target || true
cd p2-product/dependencies/
mvn clean
