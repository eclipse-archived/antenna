#!/usr/bin/env bash
# Copyright (c) Bosch Software Innovations GmbH 2018.
# Copyright (c) Bosch.IO GmbH 2020.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0

set -e

cd "$(dirname $0)/.."

failure=false
noGit=false
if [[ $1 == "--no-git" ]]; then
    noGit=true
else
    if ! type git >/dev/null 2>&1; then
        >&2 echo "This script depends on git to find out, which files are part of the source code."
        >&2 echo "Alternatively one can run it with the '--no-git' argument to scan all files. But this should only be done on a fresh or clean clone."
        exit 1
    fi
fi

testFile() {
    local file="$1"

    if [[ ! -f "$file" ]]; then
        echo "INFO: the file=[$file] has been deleted"
        return
    fi

    if ! head -15 $file | grep -q 'Copyright (c)'; then
        echo "ERROR: No copyright remark found in $file"
        failure=true
    elif ! head -15 $file | grep -q "^.*Copyright (c) .* $(date +%Y).*$"; then
        echo "ERROR: The year of your copyright remark is not correct in $file"
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
}

getFiles() {
    if [ "$noGit" = true ]; then
        find . -type f -print
    else
        git diff --name-only `git rev-parse --abbrev-ref HEAD` origin/master
    fi |
        grep -Ev '^./.git' |
        grep -Ev 'META-INF/services' |
        grep -Ev '\.(csv|rdf|ent|dtd|png|gitignore|gitattributes|md|bat|jar|json|couch|view|MF|xz|index|pdf|odt|MockMaker)' |
        grep -Ev '(gradlew|build.gradle|settings.gradle|gradle.properties|gradle/wrapper)' |
        grep -Ev '(LICENSE|NOTICE|README)' |
        grep -v 'antenna-testing/antenna-system-test/src/test/resources/analyzer'
}

while read file ; do
    testFile "$file"
done <<< "$(getFiles)"

if [ "$failure" = true ]; then
    echo "test failed"
    exit 1
fi
