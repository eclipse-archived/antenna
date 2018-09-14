/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.model;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sw360.antenna.model.xml.generated.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * Model for artifacts.
 */
public class Artifact {

    private ArtifactIdentifier artifactIdentifier = new ArtifactIdentifier();

    private DeclaredLicense declaredLicense;

    private ObservedLicense observedLicense;

    private OverridenLicense overriddenLicense;

    private LicenseInformation configuredLicense;

    private Issues securityIssues;

    private File mavenSourceJar;
    private File p2SourceJar;
    private File jar;

    private MatchState matchState;
    private boolean isProprietary;
    private String[] pathnames;
    private boolean ignoreForSourceDownload = false;

    private String copyrightStatement;
    private String modificationStatus;

    private String analysisSource;

    private boolean isAlteredByConfiguration = false;

    /**
     *
     * @return final Licenses for this artifact.
     */
    public LicenseInformation getFinalLicenses() {
        if (getConfiguredLicense().isEmpty()) {
            if (getOverriddenLicenses().isEmpty()) {
                if (!(getDeclaredLicenses().isEmpty())) {
                    return getDeclaredLicenses();
                }
            } else {
                return getOverriddenLicenses();
            }
        } else {
            return configuredLicense;
        }
        return new LicenseStatement();
    }

    public boolean isIgnoreForDownload() {
        return ignoreForSourceDownload;
    }

    public void setIgnoreForDownload(boolean ignoreForDownload) {
        this.ignoreForSourceDownload = ignoreForDownload;
    }

    public String[] getPathnames() {
        return pathnames;
    }

    public void setPathnames(String[] pathnames) {
        this.pathnames = pathnames;
        mapFilename();
    }

    public boolean isProprietary() {
        return isProprietary;
    }

    public void setProprietary(boolean isProprietary) {
        this.isProprietary = isProprietary;
    }

    public ArtifactIdentifier getArtifactIdentifier() {
        return artifactIdentifier;
    }

    public void setArtifactIdentifier(ArtifactIdentifier identifier) {
        this.artifactIdentifier = identifier;
        mapFilename();
    }

    public File getMvnSourceJar() {
        return mavenSourceJar;
    }

    public void setMavenSourceJar(File mavenSourceJar) {
        this.mavenSourceJar = mavenSourceJar;
    }

    public File getP2SourceJar() {
        return p2SourceJar;
    }

    public void setP2SourceJar(File p2SourceJar) {
        this.p2SourceJar = p2SourceJar;
    }

    public File getJar() {
        return jar;
    }

    public void setJar(File jar) {
        this.jar = jar;
    }

    /**
     *
     * @return true if Artifact has Maven or P2 Sources, false otherwise
     */
    public boolean hasSources() {
        boolean hasMvnSource = !(this.getP2SourceJar() == null);
        boolean hasP2Source = !(this.getMvnSourceJar() == null);
        boolean hasSources = hasMvnSource || hasP2Source;
        if (!hasSources) {
            hasSources = false;
        }
        return hasSources;
    }

    /**
     *
     * @return declared licenses for this artifact.
     */
    public LicenseInformation getDeclaredLicenses() {
        if (null == declaredLicense) {
            return new LicenseStatement();
        }
        JAXBElement<? extends LicenseInformation> licenseInformation = declaredLicense.getLicenseInfo();
        return licenseInformation.getValue();
    }

    public void setDeclaredLicenses(LicenseInformation declaredLicenses) {
        JAXBElement<? extends LicenseInformation> licenseInformation = new JAXBElement<>(new QName("licenseInfo"), LicenseInformation.class , declaredLicenses);
        DeclaredLicense tmpLicense = new DeclaredLicense();
        tmpLicense.setLicenseInfo(licenseInformation);
        setDeclaredLicense(tmpLicense);
    }

    public DeclaredLicense getDeclaredLicense() {
        return declaredLicense;
    }

    /**
     * Used to populate declared licenses when marshalling from the XML config
     * file.
     *
     * @param declaredLicense
     *            The declared license information from the XML config file.
     */
    public void setDeclaredLicense(DeclaredLicense declaredLicense) {
        this.declaredLicense = declaredLicense;
    }

    /**
     *
     * @return configured licenses, licenses can be configured in
     *         Sonatype
     */
    public LicenseInformation getConfiguredLicense() {
        if (null == configuredLicense) {
            return new LicenseStatement();
        }
        return configuredLicense;
    }

    public void setConfiguredLicense(LicenseInformation configuredLicense) {
        this.configuredLicense = configuredLicense;
    }

    /**
     *
     * @return observed licenses.
     */
    public LicenseInformation getObservedLicenses() {
        if (null == observedLicense) {
            return new LicenseStatement();
        }
        JAXBElement<? extends LicenseInformation> licenseInformation = this.observedLicense.getLicenseInfo();
        return licenseInformation.getValue();
    }

    public void setObservedLicenses(LicenseInformation observedLicenses) {
        JAXBElement<? extends LicenseInformation> licenseInformation = new JAXBElement<>(new QName("licenseInfo"), LicenseInformation.class , observedLicenses);
        ObservedLicense tmp = new ObservedLicense();
        tmp.setLicenseInfo(licenseInformation);
        setObservedLicense(tmp);
    }

    public ObservedLicense getObservedLicense() {
        return observedLicense;
    }

    /**
     * Used to populate observed licenses when marshalling from the XML config
     * file.
     *
     * @param observedLicense
     *            The declared license information from the XML config file.
     */
    public void setObservedLicense(ObservedLicense observedLicense) {
        this.observedLicense = observedLicense;
    }

    /**
     *
     * @return overridden licenses, licenses can be overridden in
     *         config.xml
     */
    public LicenseInformation getOverriddenLicenses() {
        if (null == overriddenLicense) {
            return new LicenseStatement();
        }
        JAXBElement<? extends LicenseInformation> licenseInformation = this.overriddenLicense.getLicenseInfo();
        return licenseInformation.getValue();
    }

    public void setOverriddenLicenses(LicenseInformation overriddenLicenses) {
        JAXBElement<? extends LicenseInformation> licenseInformation = new JAXBElement<>(new QName("licenseInfo"), LicenseInformation.class , overriddenLicenses);
        OverridenLicense tmp = new OverridenLicense();
        tmp.setLicenseInfo(licenseInformation);
        setOverriddenLicense(tmp);

        if (overriddenLicenses != null) {
            // These licenses come from configuration, so we must flag that the
            // artifact has been altered.
            setAlteredByConfiguration(true);
        }
    }

    public OverridenLicense getOverriddenLicense() {
        return overriddenLicense;
    }

    /**
     * Used to populate overridden licenses when marshalling from the XML config
     * file.
     *
     * @param overriddenLicense
     *            The declared license information from the XML config file.
     */
    public void setOverriddenLicense(OverridenLicense overriddenLicense) {
        this.overriddenLicense = overriddenLicense;
    }

    public MatchState getMatchState() {
        return matchState;
    }

    public void setMatchState(MatchState matchState) {
        this.matchState = matchState;
    }

    public String getCopyrightStatement() {
        return copyrightStatement;
    }

    public void setCopyrightStatement(String copyrightStatement) {
        this.copyrightStatement = copyrightStatement;
    }

    public String getModificationStatus() {
        return modificationStatus;
    }

    public void setModificationStatus(String modificationStatus) {
        this.modificationStatus = modificationStatus;
    }

    public String getAnalysisSource() {
        return analysisSource;
    }

    public void setAnalysisSource(String analysisSource) {
        this.analysisSource = analysisSource;
    }

    public boolean isAlteredByConfiguration() {
        return isAlteredByConfiguration;
    }

    public void setAlteredByConfiguration(boolean alteredByConfiguration) {
        isAlteredByConfiguration = alteredByConfiguration;
    }

    public Issues getSecurityIssues() {
        return securityIssues;
    }

    public void setSecurityIssues(Issues securityIssues) {
        this.securityIssues = securityIssues;
    }

    private void mapFilename() {
        if (this.artifactIdentifier != null && this.artifactIdentifier.getFilename() == null && this.pathnames != null
                && pathnames.length > 0) {
            File filePath = new File(pathnames[0]);
            this.artifactIdentifier.setFilename(filePath.getName());
        }
    }

    // CSOFF
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactIdentifier == null) ? 0 : artifactIdentifier.hashCode());
        result = prime * result + ((declaredLicense == null) ? 0 : declaredLicense.hashCode());
        result = prime * result + (ignoreForSourceDownload ? 1231 : 1237);
        result = prime * result + (isProprietary ? 1231 : 1237);
        result = prime * result + ((jar == null) ? 0 : jar.hashCode());
        result = prime * result + ((matchState == null) ? 0 : matchState.hashCode());
        result = prime * result + ((mavenSourceJar == null) ? 0 : mavenSourceJar.hashCode());
        result = prime * result + ((observedLicense == null) ? 0 : observedLicense.hashCode());
        result = prime * result + ((overriddenLicense == null) ? 0 : overriddenLicense.hashCode());
        result = prime * result + ((p2SourceJar == null) ? 0 : p2SourceJar.hashCode());
        result = prime * result + Arrays.hashCode(pathnames);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Artifact other = (Artifact) obj;
        if (artifactIdentifier == null) {
            if (other.artifactIdentifier != null) {
                return false;
            }
        } else if (!artifactIdentifier.equals(other.artifactIdentifier)) {
            return false;
        }
        if (declaredLicense == null) {
            if (other.declaredLicense != null) {
                return false;
            }
        } else if (!declaredLicense.equals(other.declaredLicense)) {
            return false;
        }
        if (ignoreForSourceDownload != other.ignoreForSourceDownload) {
            return false;
        }
        if (isProprietary != other.isProprietary) {
            return false;
        }
        if (jar == null) {
            if (other.jar != null) {
                return false;
            }
        } else if (!jar.equals(other.jar)) {
            return false;
        }
        if (matchState != other.matchState) {
            return false;
        }
        if (mavenSourceJar == null) {
            if (other.mavenSourceJar != null) {
                return false;
            }
        } else if (!mavenSourceJar.equals(other.mavenSourceJar)) {
            return false;
        }
        if (observedLicense == null) {
            if (other.observedLicense != null) {
                return false;
            }
        } else if (!observedLicense.equals(other.observedLicense)) {
            return false;
        }
        if (overriddenLicense == null) {
            if (other.overriddenLicense != null) {
                return false;
            }
        } else if (!overriddenLicense.equals(other.overriddenLicense)) {
            return false;
        }
        if (p2SourceJar == null) {
            if (other.p2SourceJar != null) {
                return false;
            }
        } else if (!p2SourceJar.equals(other.p2SourceJar)) {
            return false;
        }
        if (!Arrays.equals(pathnames, other.pathnames)) {
            return false;
        }
        return true;
    }
    // CSON
}
