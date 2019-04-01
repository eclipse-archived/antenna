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
package org.eclipse.sw360.antenna.api.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Handles configuration properties that control Antenna's setup.
 */
public class ToolConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolConfiguration.class);

    private static final String DEPENDENCIES_DIR = "dependencies";

    private final Path antennaTargetDirectory;

    private List<String> filesToAttach;
    private boolean attachAll;
    private boolean skipAntennaExecution;
    private List<File> configFiles;
    private List<URI> configFileUris;
    private String productName;
    private final String productFullName;
    private String version;
    private Path scanDir;
    private boolean isMavenInstalled;
    private String companyName;
    private String copyrightHoldersName;
    private String copyrightNotice;
    private String disclosureDocumentNotes;
    private Workflow workflow;
    private boolean showCopyrightStatements;
    private Charset encodingCharSet;
    private final String proxyHost;
    private final int proxyPort;
    private final boolean useProxy;

    private <T> List<T> makeUnmodifiable(List<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    public ToolConfiguration(ConfigurationBuilder builder) {
        this.antennaTargetDirectory = builder.antennaTargetDirectory;
        this.filesToAttach = makeUnmodifiable(builder.filesToAttach);
        this.attachAll = builder.attachAll;
        this.skipAntennaExecution = builder.skipAntennaExecution;
        this.configFiles = makeUnmodifiable(builder.configFiles);
        this.configFileUris = makeUnmodifiable(builder.configFileUris);
        this.productName = builder.productName;
        this.productFullName = builder.productFullName;
        this.version = builder.version;
        this.scanDir = builder.scanDir;
        this.isMavenInstalled = builder.isMavenInstalled;
        this.companyName = builder.companyName;
        this.copyrightHoldersName = builder.copyrightHoldersName;
        this.copyrightNotice = builder.copyrightNotice;
        this.disclosureDocumentNotes = builder.disclosureDocumentNotes;
        this.workflow = builder.workflow;
        this.showCopyrightStatements = builder.showCopyrightStatements;
        if(builder.encodingCharSet != null) {
            this.encodingCharSet = builder.encodingCharSet;
        }else{
            this.encodingCharSet = StandardCharsets.UTF_8;
        }
        this.proxyHost = builder.proxyHost;
        this.proxyPort = builder.proxyPort;
        this.useProxy = this.proxyHost != null && !"".equals(this.proxyHost) && this.proxyPort > 0;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public Path getAntennaTargetDirectory() {
        return this.antennaTargetDirectory;
    }

    public Path getDependenciesDirectory() {
        return this.antennaTargetDirectory.resolve(DEPENDENCIES_DIR);
    }

    public List<String> getFilesToAttach() {
        return this.filesToAttach;
    }

    public List<File> getConfigFiles() {
        return this.configFiles;
    }

    public List<URI> getConfigFileUris() {
        return this.configFileUris;
    }

    public String getProductName() {
        return this.productName;
    }

    public String getProductFullName() {
        return this.productFullName;
    }

    public String getVersion() {
        return this.version;
    }

    public Path getScanDir() {
        return this.scanDir;
    }

    public boolean isAttachAll() {
        return this.attachAll;
    }

    public boolean isSkipAntennaExecution() { return this.skipAntennaExecution; }

    public boolean isMavenInstalled() {
        return isMavenInstalled;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCopyrightHoldersName() {
        return copyrightHoldersName;
    }

    public String getCopyrightNotice() {
        return copyrightNotice;
    }

    public String getDisclosureDocumentNotes() {
        return disclosureDocumentNotes;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public Charset getEncoding() {
        return encodingCharSet;
    }

    public boolean isShowCopyrightStatements() {
        return showCopyrightStatements;
    }

    public boolean useProxy() {
        return useProxy;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public static class ConfigurationBuilder {
        private Path antennaTargetDirectory;

        private List<String> filesToAttach;
        private boolean attachAll;
        private boolean skipAntennaExecution;
        private List<File> configFiles;
        private List<URI> configFileUris;
        private String productName;
        private String productFullName;
        private String version;
        private Path scanDir;
        private boolean isMavenInstalled;
        private String companyName;
        private String copyrightHoldersName;
        private String copyrightNotice;
        private String disclosureDocumentNotes;
        private Workflow workflow;
        private boolean showCopyrightStatements;
        private Charset encodingCharSet;
        private String proxyHost;
        private int proxyPort;

        public ConfigurationBuilder setFilesToAttach(List<String> filesToAttach) {
            this.filesToAttach = filesToAttach;
            return this;
        }

        public ConfigurationBuilder setAntennaTargetDirectory(String antennaTargetDirectory) {
            return this.setAntennaTargetDirectory(new File(antennaTargetDirectory).toPath());
        }

        private ConfigurationBuilder setAntennaTargetDirectory(Path antennaTargetDirectory) {
            this.antennaTargetDirectory = antennaTargetDirectory;
            return this;
        }

        public ConfigurationBuilder setSkipAntennaExecution(boolean skipAntennaExecution) {
            this.skipAntennaExecution = skipAntennaExecution;
            return this;
        }

        public ConfigurationBuilder setAttachAll(boolean attachAll) {
            this.attachAll = attachAll;
            return this;
        }

        public ConfigurationBuilder setConfigFiles(List<File> configFiles) {
            this.configFiles = configFiles;
            return this;
        }

        public ConfigurationBuilder setConfigFileUris(List<URI> configFileUris) {
            this.configFileUris = configFileUris;
            return this;
        }

        public ConfigurationBuilder setProductName(String productName) {
            this.productName = productName;
            return this;
        }

        public ConfigurationBuilder setProductFullName(String productFullname) {
            this.productFullName = productFullname;
            return this;
        }

        public ConfigurationBuilder setVersion(String version) {
            this.version = version;
            return this;
        }

        public ConfigurationBuilder setScanDir(String scanDir) {
            if (scanDir == null) {
                return this;
            }
            return this.setScanDir(new File(scanDir).toPath());
        }

        private ConfigurationBuilder setScanDir(Path scanDir) {
            this.scanDir = scanDir;
            return this;
        }

        public ConfigurationBuilder setMavenInstalled(boolean mavenInstalled) {
            this.isMavenInstalled = mavenInstalled;
            return this;
        }

        public ConfigurationBuilder setCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public ConfigurationBuilder setCopyrightHoldersName(String copyrightHoldersName) {
            if (StringUtils.isBlank(copyrightHoldersName)) {
                this.copyrightHoldersName = "The copyright holder";
            } else {
                this.copyrightHoldersName = copyrightHoldersName;
            }
            return this;
        }

        public ConfigurationBuilder setCopyrightNotice(String copyrightNotice) {
            this.copyrightNotice = copyrightNotice;
            return this;
        }

        public ConfigurationBuilder setDisclosureDocumentNotes(String disclosureDocumentNotes) {
            this.disclosureDocumentNotes = disclosureDocumentNotes;
            return this;
        }

        public ConfigurationBuilder setWorkflow(Workflow workflow) {
            this.workflow = workflow;
            return this;
        }

        public ConfigurationBuilder setEncoding(String encoding) {
            Charset charSetEncoding;
            try {
                charSetEncoding = Charset.forName(encoding);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Could not find the encoding {}. Falling back to VM default", encoding);
                charSetEncoding = Charset.defaultCharset();
            }
            this.encodingCharSet = charSetEncoding;
            return this;
        }

        public ConfigurationBuilder setShowCopyrightStatements(boolean showCopyrightStatements) {
            this.showCopyrightStatements = showCopyrightStatements;
            return this;
        }

        public ConfigurationBuilder setProxyHost(String host) {
            this.proxyHost = host;
            return this;
        }

        public ConfigurationBuilder setProxyPort(int port) {
            this.proxyPort = port;
            return this;
        }

        public ToolConfiguration buildConfiguration() {
            return new ToolConfiguration(this);
        }

    }
}
