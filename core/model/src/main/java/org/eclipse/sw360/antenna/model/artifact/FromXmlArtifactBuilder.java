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
import org.eclipse.sw360.antenna.model.coordinates.*;
import org.eclipse.sw360.antenna.model.license.FromXmlLicenseInformationBuilder;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.DeclaredLicense;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import java.util.Objects;
import java.util.stream.Stream;

@XmlAccessorType(XmlAccessType.FIELD)
public class FromXmlArtifactBuilder implements IArtifactBuilder {
    protected MavenCoordinatesBuilder mavenCoordinates;
    protected BundleCoordinatesBuilder bundleCoordinates;
    protected JavaScriptCoordinatesBuilder javaScriptCoordinates;
    protected DotNetCoordinatesBuilder dotNetCoordinates;
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
                .addFact(new CopyrightStatement(copyrightStatement))
                .addFact(new ArtifactMatchingMetadata(matchState))
                .addFact(new ArtifactModificationStatus(modificationStatus));
        Stream.of(mavenCoordinates,
                bundleCoordinates,
                javaScriptCoordinates,
                dotNetCoordinates)
                .filter(Objects::nonNull)
                .map(CoordinatesBuilder::build)
                .forEach(artifact::addCoordinate);
        if(declaredLicense != null) {
            final LicenseInformation licenseInfo =
                    ((FromXmlLicenseInformationBuilder.ILicenseInformationBuilder) declaredLicense.getLicenseInfo()
                            .getValue()).build();
            artifact.addFact(new DeclaredLicenseInformation(licenseInfo));
        }
        if(isProprietary != null) {
            artifact.setProprietary(isProprietary);
        }

        return artifact;
    }

    public interface CoordinatesBuilder {
        Coordinate build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MavenCoordinatesBuilder implements CoordinatesBuilder {

        private String artifactId;
        private String groupId;
        private String version;

        public MavenCoordinatesBuilder setArtifactId(String value) {
            this.artifactId = value;
            return this;
        }

        public MavenCoordinatesBuilder setGroupId(String value) {
            this.groupId = value;
            return this;
        }

        public MavenCoordinatesBuilder setVersion(String value) {
            this.version = value;
            return this;
        }

        @Override
        public Coordinate build() {
            return new Coordinate(Coordinate.Types.MAVEN, groupId, artifactId, version);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class BundleCoordinatesBuilder implements CoordinatesBuilder {
        protected String symbolicName;
        protected String bundleVersion;

        public void setSymbolicName(String value) {
            this.symbolicName = value;
        }

        public void setBundleVersion(String value) {
            this.bundleVersion = value;
        }

        @Override
        public Coordinate build() {
            return new Coordinate(Coordinate.Types.P2, symbolicName, bundleVersion);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class JavaScriptCoordinatesBuilder implements CoordinatesBuilder {
        protected String namespace;
        protected String packageName;
        protected String version;

        public JavaScriptCoordinatesBuilder setNamespace(String value) {
            this.namespace = value;
            return this;
        }

        public JavaScriptCoordinatesBuilder setPackageName(String value) {
            this.packageName = value;
            return this;
        }

        public JavaScriptCoordinatesBuilder setVersion(String value) {
            this.version = value;
            return this;
        }

        @Override
        public Coordinate build() {
            return new Coordinate(Coordinate.Types.NPM, namespace, packageName, version); // TODO: this is broken
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DotNetCoordinatesBuilder implements CoordinatesBuilder {
        protected String packageId;
        protected String version;

        public DotNetCoordinatesBuilder setPackageId(String value) {
            this.packageId = value;
            return this;
        }

        public DotNetCoordinatesBuilder setVersion(String value) {
            this.version = value;
            return this;
        }

        @Override
        public Coordinate build() {
            return new Coordinate(Coordinate.Types.NUGET, packageId, version);
        }
    }
}
