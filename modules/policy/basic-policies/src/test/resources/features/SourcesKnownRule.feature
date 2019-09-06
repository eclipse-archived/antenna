#
# Copyright (c) Bosch Software Innovations GmbH 2019.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#

Feature: Software link available, either local or remote
  Artifacts have to know a link to the source code of the component,
  either a local existing file or a link to the sources in the source repo.

    Scenario: Artifact without Software information fails the rule
      Given an artifact
      When I use the rule "SourcesKnown"
      Then the artifact should fail with policy id "SourcesKnown"

    Scenario: Proprietary artifact passes the rule
      Given an artifact with
        | proprietary | true |
      When I use the rule "SourcesKnown"
      Then no artifact fails on policy id "SourcesKnown"

    Scenario: Artifact with file information pass the rule
      Given an artifact with
        | sources | file:///localfile.zip |
      When I use the rule "SourcesKnown" 
      Then no artifact fails on policy id "SourcesKnown"

    Scenario: Artifact with http link source information pass the rule
      Given an artifact with
        | sources | https://github.com/eclipse/antenna/tree/1.0.0-RC5 |
      When I use the rule "SourcesKnown" 
      Then no artifact fails on policy id "SourcesKnown"