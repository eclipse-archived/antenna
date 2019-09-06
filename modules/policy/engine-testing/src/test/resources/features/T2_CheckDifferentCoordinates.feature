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

Feature: Check Multiple Coordinate Types
  In this test we do not allow components from different technologies in the same project

    Scenario: Artifacts from different technologies fail
      Given an artifact called "Maven" with
        | coordinates | pkg:maven/com.something/foo@1.0.0 |
      And an artifact called "Nuget" with
        | coordinates | pkg:nuget/bar@2.3.1 |
      When I use the rule "T2"
      Then all artifacts fail with policy id "T2"

    Scenario: Artifacts from same technology succeed
      Given an artifact called "Maven" with
        | coordinates | pkg:maven/com.something/foo@1.0.0 |
      And an artifact called "Maven2" with
        | coordinates | pkg:maven/com.something/bar@2.3.1 |
      When I use the rule "T2"
      Then no artifact fails


