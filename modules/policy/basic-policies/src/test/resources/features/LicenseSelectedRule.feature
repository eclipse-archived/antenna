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

Feature: License Selection has been done
  An artifact has a declared license expression that allows a license selection.
  At this stage of the artifact handling this selection has to be executed and the or has
  to resolved

    Scenario: Artifact with no license information pass the rule, because no license is ok
      Given an artifact
      When I use the rule "LicenseSelected"
      Then no artifact fails on policy id "LicenseSelected"

    Scenario: Proprietary artifact passes the rule
      Given an artifact with
        | proprietary | true |
      When I use the rule "LicenseSelected"
      Then no artifact fails on policy id "LicenseSelected"

    Scenario: Artifact with a single license pass the rule
      Given an artifact with
        | licenses | EPL-2.0 | Declared |
      When I use the rule "LicenseSelected"
      Then no artifact fails on policy id "LicenseSelected"
    
    Scenario: Artifact with an and expression pass the rule
      Given an artifact with
        | licenses | EPL-2.0 AND Apache-2.0 | Declared |
      When I use the rule "LicenseSelected"
      Then no artifact fails on policy id "LicenseSelected"
    
    Scenario: Artifact with an or expression fails the rule
      Given an artifact with
        | licenses | EPL-2.0 OR Apache-2.0 | Declared |
      When I use the rule "LicenseSelected"
      Then the artifact should fail with policy id "LicenseSelected"
    
    Scenario: Artifact with an or expression in lower case fails the rule
      Given an artifact with
        | licenses | EPL-2.0 or Apache-2.0 | Declared |
      When I use the rule "LicenseSelected"
      Then the artifact should fail with policy id "LicenseSelected"
    
    Scenario: Artifact with an or expression but selected license pass the rule
      Given an artifact with
        | licenses | EPL-2.0 OR Apache-2.0 | Declared |
        | licenses | EPL-2.0 | Configured |
      When I use the rule "LicenseSelected"
      Then no artifact fails on policy id "LicenseSelected"