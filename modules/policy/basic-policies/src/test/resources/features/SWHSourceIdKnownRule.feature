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

Feature: Software Heritage link available
  Artifacts should have a link to the sources on software heritage.

    Scenario: Artifact without Software Heritage information fails the rule
      Given an artifact
      When I use the rule "SWHAvailable"
      Then the artifact should fail with policy id "SWHAvailable"

    Scenario: Proprietary artifact passes the rule
      Given an artifact with
        | proprietary | true |
      When I use the rule "SWHAvailable"
      Then no artifact fails on policy id "SWHAvailable"

    Scenario: Artifact with Software Heritage information pass the rule
      Given an artifact with
        | sources | swh:1:rel:22ece559cc7cc2364edc5e5593d63ae8bd229f9f |
      When I use the rule "SWHAvailable" 
      Then no artifact fails on policy id "SWHAvailable"
