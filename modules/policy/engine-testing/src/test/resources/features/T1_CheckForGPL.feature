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

Feature: Identify usage of GPL license
  GPL is a copyleft license which can not be used in this context

  Scenario: Artifact with GPL or one of three other licenses passes the test
    Given an artifact called "EPL" with
      | licenses | EPL-1.0 | Declared |
    And an artifact called "Apache 2.0" with
      | licenses | Apache-2.0 | Declared |
    And an artifact called "GPL" with
      | licenses | GPL-2.0-only | Declared |
    When I use the rule "T1"
    Then the artifact called "GPL" should fail with policy id "T1"

  Scenario: Artifact with GPL should fail
    Given an artifact with
      | licenses | AGPL-3.0-or-later | Declared |
    When I use the rule "T1"
    Then all artifacts fail with policy id "T1"

  Scenario: Artifact without GPL should fail
    Given an artifact with
      | licenses | EPL-1.0 | Declared |
    When I use the rule "T1"
    Then no artifact fails
