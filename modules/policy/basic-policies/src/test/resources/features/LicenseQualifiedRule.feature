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

Feature: License Metadata is Qualified
  For a License the license text has to be available to process components properly.

    Scenario: Artifact with no license information pass the rule, because no license information is broken
      Given an artifact
      When I use the rule "LicenseQualified"
      Then no artifact fails on policy id "LicenseQualified"

    Scenario: Proprietary artifact passes the rule
      Given an artifact with
        | proprietary | true |
      When I use the rule "LicenseQualified"
      Then no artifact fails on policy id "LicenseQualified"

    Scenario: Artifact with only the license id fails the rule
      Given an artifact with
        | licenses | Apache-2.0 | Declared |
      When I use the rule "LicenseQualified"
      Then the artifact should fail with policy id "LicenseQualified"

    Scenario: Artifact with license including license text pass the rule
      Given an artifact with
        | licenses | Apache-2.0::My Apache license text | Declared |
      When I use the rule "LicenseQualified"
      Then no artifact fails on policy id "LicenseQualified"
