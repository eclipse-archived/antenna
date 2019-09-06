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

Feature: Artifact Identified
  Artifacts need to be identified in order to process them properly.
  This is represented by coordinates in a specific technology.

  Scenario: Artifact without coordinates fails the rule
    Given an artifact
    When I use the rule "ArtifactIdentified"
    Then the artifact should fail with policy id "ArtifactIdentified"

  Scenario: Proprietary artifact passes the rule
    Given an artifact with
      | proprietary | true |
    When I use the rule "ArtifactIdentified"
    Then no artifact fails on policy id "ArtifactIdentified"

  Scenario: Artifact with generic coordinates fails the rule
    Given an artifact with
      | coordinates | pkg:generic/some_component@1.0.0 |
    When I use the rule "ArtifactIdentified"
    Then the artifact should fail with policy id "ArtifactIdentified"

  Scenario: Artifact with maven coordinates pass the rule
    Given an artifact with
      | coordinates | pkg:maven/org.foo/bar@1.0.0 |
    When I use the rule "ArtifactIdentified"
    Then no artifact fails on policy id "ArtifactIdentified"

  Scenario: Artifact with p2 coordinates pass the rule
    Given an artifact with
      | coordinates | pkg:p2/org.foo.bar@1.0.0 |
    When I use the rule "ArtifactIdentified"
    Then no artifact fails on policy id "ArtifactIdentified"

  Scenario: Artifact with java script coordinates pass the rule
    Given an artifact with
      | coordinates | pkg:npm/%40angular/animation@12.3.1 |
    When I use the rule "ArtifactIdentified"
    Then no artifact fails on policy id "ArtifactIdentified"
