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

CURRENT_TAG="$(git describe --tag)"

createDocumentation() {
  "${BASEDIR}"/execute_github_site_maven_plugin.sh "$CURRENT_TAG"
}

createNewIndexRootFile() {
  fetchGhPages
  "${BASEDIR}"/writeIndexRootFile.sh
  pushGhPages
}

fetchGhPages() {
  git fetch "https://github.com/eclipse/antenna.git" gh-pages:gh-pages
}

pushGhPages() {
  if ! grep -E '^[[:alnum:]]*$' <<< "$GITHUB_CRENDETIALS_PSW"; then
    echo "WARN: password might contain special characters, which generate problems in the following URL."
  fi
  if ! grep -E '^[[:alnum:]]*$' <<< "$GITHUB_CRENDETIALS_USR"; then
    echo 'WARN: username might contain special characters, which generate problems in the following URL.'
  fi
  git push "https://${GITHUB_CREDENTIALS_USR}:${GITHUB_CREDENTIALS_PSW}@github.com/eclipse/antenna.git" gh-pages

}

createDocumentation
createNewIndexRootFile
