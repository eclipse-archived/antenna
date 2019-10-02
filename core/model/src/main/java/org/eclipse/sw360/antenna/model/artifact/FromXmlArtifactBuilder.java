/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.model.artifact;

import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.license.FromXmlLicenseInformationConverter;
import org.eclipse.sw360.antenna.model.xml.generated.DeclaredLicense;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlAccessorType(XmlAccessType.FIELD)
public class FromXmlArtifactBuilder implements IArtifactBuilder {
    protected MavenCoordinates.MavenCoordinatesBuilder mavenCoordinates;
    protected BundleCoordinates.BundleCoordinatesBuilder bundleCoordinates;
    protected JavaScriptCoordinates.JavaScriptCoordinatesBuilder javaScriptCoordinates;
    protected DotNetCoordinates.DotNetCoordinatesBuilder dotNetCoordinates;
    protected String filename;
    protected String hash;
    protected DeclaredLicense declaredLicense;
    protected Boolean isProprietary;
    @XmlSchemaType(name = "string")
    protected MatchState matchState;
    protected String copyrightStatement;
    protected String modificationStatus;

    @Override
    public Artifact build() {
        final Artifact artifact = new Artifact("from XML")
                .addFact(new ArtifactFilename(filename, hash))
                .addFact(mavenCoordinates)
                .addFact(bundleCoordinates)
                .addFact(javaScriptCoordinates)
                .addFact(dotNetCoordinates)
                .addFact(new CopyrightStatement(copyrightStatement))
                .addFact(new ArtifactMatchingMetadata(matchState))
                .addFact(new ArtifactModificationStatus(modificationStatus));
        if(declaredLicense != null) {
            final LicenseInformation licenseInfo = declaredLicense.getLicenseInfo().getValue();
            artifact.addFact(new DeclaredLicenseInformation(FromXmlLicenseInformationConverter.convert(licenseInfo)));
        }
        if(isProprietary != null) {
            artifact.setProprietary(isProprietary);
        }

        return artifact;
    }
}
