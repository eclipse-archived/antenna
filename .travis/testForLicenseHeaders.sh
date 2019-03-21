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
    if ! head -15 $file | grep -q 'Copyright (c)'; then
        echo "ERROR: No copyright remark found in $file"
        failure=true
    fi
    if ! head -15 $file | grep -q 'SPDX-License-Identifier:'; then
        echo "ERROR: no 'SPDX-License-Identifier' in $file"
        failure=true
    fi
    if head -15 $file | grep -q 'proprietary'; then
        echo "ERROR: $file might be licensed proprietarily"
        failure=true
    fi
    if ! head -15 $file | grep -q 'http://www.eclipse.org/legal/epl-v20.html'; then
        echo "ERROR: epl-2.0 header is not found in $file"
        failure=true
    fi
done <<< "$(git ls-files \
    | grep -Ev '\.(csv|rdf|ent|dtd|png|gitignore|gitattributes|md|bat|jar|json|couch|view)' \
    | grep -Ev '(gradlew|build.gradle|settings.gradle|gradle.properties|gradle/wrapper)' \
    | grep -Ev '(LICENSE|NOTICE|README)' \
    | grep -v 'antenna-testing/antenna-system-test/src/test/resources/analyzer')"

if [ "$failure" = true ]; then
    echo "test failed"
    exit 1
fi
