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

Feature: Matchstate can be a problem
  Unknown matchstate is a problem, because we don't know what's up

  Scenario: Unknown Matchstate fails the test
    Given an artifact with
      | matchstate | unknown |
    When I use the rule "UnknownArtifactRule"
    Then the artifact should fail with policy id "Unknown"

  Scenario: Exact Matchstate is good
    Given an artifact with
      | matchstate | exact |
    When I use the rule "UnknownArtifactRule"
    Then no artifact fails

  Scenario: Matchstate rules fail selectively
    Given an artifact called "Passing" with
      | matchstate | similar |
    And an artifact called "Failing" with
      | matchstate | unknown |
    When I use the rule "UnknownArtifactRule"
    Then the artifact called "Failing" should fail with policy id "Unknown"
