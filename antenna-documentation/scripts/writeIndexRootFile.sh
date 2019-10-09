#!/usr/bin/env bash -e
# Copyright (c) Bosch Software Innovations GmbH 2019.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0

BASEDIR=$(readlink -f "$0")
BASEDIR=$(dirname "$BASEDIR")

writeIndexFile() {
  echo '<!DOCTYPE html>
  <html lang="en">
  <head>
      <meta charset="utf-8">
      <title>SW360 Antenna Documentation</title>
  </head>
  <body>
  <h1>SW360 Antenna Documentation</h1>
  <p>The following versions of our documentation are available:</p>
  <ol>' > index.html

  for DIRECTORY in */
  do
    dir=${DIRECTORY%*/}
    echo "<li><a href='https://eclipse.github.io/antenna/${dir##*/}'>${dir##*/}</a></li>" >> index.html
  done

  echo '</ol>
  <p>If you want to see our project code site checkout
  <a href="https://github.com/eclipse/antenna">this link</a>.</p>
  <p>Or for more information about this project, check out our
  <a href="https://eclipse.org/antenna">project website</a>.</p></body></html>' >> index.html
}

CURRENT_BRANCH=$(git branch | grep \* | cut -d ' ' -f2)

# checkout gh-pages branch and delete untracked files and directories
git checkout gh-pages
git clean -fd

writeIndexFile
# if there is a difference after writing the index html, commit
if ! git diff-index --quiet HEAD --; then
  git add index.html
  git commit -m "update index file to be according to new version ${CURRENT_BRANCH}" -s
fi

git checkout "${CURRENT_BRANCH}"
