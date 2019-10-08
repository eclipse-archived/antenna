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

Feature: Sources available as file locally
  Artifacts must have a local file with the sources, if a source bundle has to be provided.

    Scenario: Artifact without Software information fails the rule
      Given an artifact
      When I use the rule "SourcesAvailable"
      Then the artifact should fail with policy id "SourcesAvailable"

    Scenario: Proprietary artifact passes the rule
      Given an artifact with
        | proprietary | true |
      When I use the rule "SourcesAvailable"
      Then no artifact fails on policy id "SourcesAvailable"

    Scenario: Artifact with file information and existing file pass the rule
      Given an artifact with
        | sources | file:///localfile.zip | true |
      When I use the rule "SourcesAvailable" 
      Then no artifact fails on policy id "SourcesAvailable"

    Scenario: Artifact with file information and non-existing file fails the rule
      Given an artifact with
        | sources | file:///localfile.zip | false |
      When I use the rule "SourcesAvailable" 
      Then the artifact should fail with policy id "SourcesAvailable"

    Scenario: Artifact with http link source information pass the rule
      Given an artifact with
        | sources | https://github.com/eclipse/antenna/tree/1.0.0-RC5 |
      When I use the rule "SourcesAvailable" 
      Then the artifact should fail with policy id "SourcesAvailable"