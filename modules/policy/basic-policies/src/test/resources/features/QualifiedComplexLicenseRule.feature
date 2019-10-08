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

Feature: Complex License Expression is Qualified
  For an artifact with a complex license expression (more than 2 licenses involved),
  it is recommended to explicitely evaluate the licenses and express that as overwritten
  license expression, instead of letting the automated license calculation happen.

    Scenario: Artifact with no license information pass the rule, because license is not complex
      Given an artifact
      When I use the rule "QualifiedComplexLicense"
      Then no artifact fails on policy id "QualifiedComplexLicense"
    
    Scenario: Proprietary artifact passes the rule
      Given an artifact with
        | proprietary | true |
      When I use the rule "QualifiedComplexLicense"
      Then no artifact fails on policy id "QualifiedComplexLicense"

    Scenario: Artifact with a single license pass the rule
      Given an artifact with
        | licenses | EPL-2.0 | Declared |
      When I use the rule "QualifiedComplexLicense"
      Then no artifact fails on policy id "QualifiedComplexLicense"
    
    Scenario: Artifact with two licenses involved pass the rule
      Given an artifact with
        | licenses | EPL-2.0 | Declared |
        | licenses | EPL-2.0 AND MIT | Observed |
      When I use the rule "QualifiedComplexLicense"
      Then no artifact fails on policy id "QualifiedComplexLicense"

    Scenario: Artifact with three licenses involved and not overwritten fails the rule
      Given an artifact with
        | licenses | EPL-2.0 AND MIT AND Apache-2.0 | Observed |
      When I use the rule "QualifiedComplexLicense"
      Then the artifact should fail with policy id "QualifiedComplexLicense"

    Scenario: Artifact with three licenses involved with have been approved pass the rule
      Given an artifact with
        | licenses | EPL-2.0 AND MIT AND Apache-2.0 | Observed |
        | licenses | EPL-2.0 AND MIT AND Apache-2.0 | Overwritten |
      When I use the rule "QualifiedComplexLicense"
      Then no artifact fails on policy id "QualifiedComplexLicense"
