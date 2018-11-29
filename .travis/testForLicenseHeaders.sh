#!/usr/bin/env bash
# Copyright (c) Bosch Software Innovations GmbH 2018.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0

cd "$(dirname $0)/.."

failure=false

while read file ; do
    if ! head -15 $file | grep -q 'SPDX-License-Identifier:' $file; then
        echo "WARN: no 'SPDX-License-Identifier' in  $file"
    fi
    if head -15 $file | grep -q 'http://www.eclipse.org/legal/epl-v20.html'; then
        continue # epl found
    fi

    echo "$(tput bold)ERROR: epl-2.0 header is not found in $file$(tput sgr0)"
    failure=true
done <<< "$(git ls-files \
    | grep -Ev '\.(csv|rdf|ent|dtd|png|gitignore|gitattributes|md|bat|jar|json|couch|view)' \
    | grep -Ev '(gradlew|build.gradle|settings.gradle|gradle.properties|gradle/wrapper)' \
    | grep -Ev '(LICENSE|NOTICE|README)' \
    | grep -v 'antenna-testing/antenna-system-test/src/test/resources/analyzer')"

if [ "$failure" = true ]; then
    echo "test failed"
    exit 1
fi
