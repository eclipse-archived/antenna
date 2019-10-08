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

Feature: License Expression is Qualified
  For an artifact, either Declared and Observed license information is available,
  or the license expression is overwritten.

    Scenario: Artifact with no license information fails the rule
      Given an artifact
      When I use the rule "ArtifactLicenseQualified"
      Then the artifact should fail with policy id "ArtifactLicenseQualified"
    
    Scenario: Proprietary artifact passes the rule
      Given an artifact with
        | proprietary | true |
      When I use the rule "ArtifactLicenseQualified"
      Then no artifact fails on policy id "ArtifactLicenseQualified"

   Scenario: Artifact with a declared license only fails the rule
      Given an artifact with
        | licenses | EPL-2.0 | Declared |
      When I use the rule "ArtifactLicenseQualified"
      Then the artifact should fail with policy id "ArtifactLicenseQualified"
    
    Scenario: Artifact with an observed license only fails the rule
      Given an artifact with
        | licenses | EPL-2.0 AND MIT | Observed |
      When I use the rule "ArtifactLicenseQualified"
      Then the artifact should fail with policy id "ArtifactLicenseQualified"

    Scenario: Artifact with declared and observed license pass the rule
      Given an artifact with
        | licenses | EPL-2.0 | Declared |
        | licenses | EPL-2.0 AND MIT | Observed |
      When I use the rule "ArtifactLicenseQualified"
      Then no artifact fails on policy id "ArtifactLicenseQualified"

    Scenario: Artifact with overwritten license pass the rule
      Given an artifact with
        | licenses | EPL-2.0 | Observed |
        | licenses | EPL-2.0 | Overwritten |
      When I use the rule "ArtifactLicenseQualified"
      Then no artifact fails on policy id "ArtifactLicenseQualified"
