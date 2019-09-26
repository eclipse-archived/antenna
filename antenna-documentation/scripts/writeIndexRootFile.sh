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

generateIndexFile() {
  cat <<EOF
  <!DOCTYPE html>
  <html lang="en">
      <head>
          <meta charset="utf-8">
          <title>SW360 Antenna Documentation</title>
      </head>
      <body>
          <h1>SW360 Antenna Documentation</h1>
          <p>The following versions of our documentation are available:</p>
          <ol>
EOF

  for DIRECTORY in */
  do
    dir=$(basename "$DIRECTORY")
    echo "          <li><a href='https://eclipse.github.io/antenna/${dir}'>${dir}</a></li>"
  done

  cat <<EOF
          </ol>
          <p>This project is hosted on GitHub as
              <a href="https://github.com/eclipse/antenna">eclipse/antenna</a>.
          </p>
          <p>For more information about this project, check out our
              <a href="https://eclipse.org/antenna">project website</a>.
          </p>
      </body>
  </html>
EOF
}

CURRENT_COMMIT=$(git describe --tag)

# checkout gh-pages branch and delete untracked files and directories
git checkout gh-pages
git clean -fd

generateIndexFile | tee index.html
# if there is a difference after writing the index html, commit
if ! git diff-index HEAD; then
  git add index.html
  git commit -m "update index file to be according to new version ${CURRENT_COMMIT}" -s
fi

git checkout "${CURRENT_COMMIT}"
