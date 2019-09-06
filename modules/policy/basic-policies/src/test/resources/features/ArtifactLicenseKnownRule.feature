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

Feature: License Known
  License information is available for the artifact

    Scenario: Artifact with no license information fails the rule
      Given an artifact
      When I use the rule "ArtifactLicenseKnown"
      Then the artifact should fail with policy id "ArtifactLicenseKnown"

    Scenario: Proprietary artifact passes the rule
      Given an artifact with
        | proprietary | true |
      When I use the rule "ArtifactLicenseKnown"
      Then no artifact fails on policy id "ArtifactLicenseKnown"

    Scenario: Artifact with declared license pass the rule
      Given an artifact with
        | licenses | Apache-2.0 | Declared |
      When I use the rule "ArtifactLicenseKnown"
      Then no artifact fails on policy id "ArtifactLicenseKnown"

    Scenario: Artifact with observed license pass the rule
      Given an artifact with
        | licenses | Apache-2.0 | Observed |
      When I use the rule "ArtifactLicenseKnown"
      Then no artifact fails on policy id "ArtifactLicenseKnown"

    Scenario: Artifact with overwritten license pass the rule
      Given an artifact with
        | licenses | Apache-2.0 | Overwritten |
      When I use the rule "ArtifactLicenseKnown"
      Then no artifact fails on policy id "ArtifactLicenseKnown"

    Scenario: Artifact with configured license pass the rule
      Given an artifact with
        | licenses | Apache-2.0 | Configured |
      When I use the rule "ArtifactLicenseKnown"
      Then no artifact fails on policy id "ArtifactLicenseKnown"
