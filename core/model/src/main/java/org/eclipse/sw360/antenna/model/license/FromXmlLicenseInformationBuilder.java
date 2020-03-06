/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.model.license;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FromXmlLicenseInformationBuilder {
    public interface ILicenseInformationBuilder {
        LicenseInformation build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "licenseInformation")
    public abstract static class AbstractLicenseInformation {
        public abstract String evaluate();
        public abstract boolean isEmpty();
        public abstract List<License> getLicenses();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "licenseStatement", propOrder = {
            "licenseInfo"
    })
    public static class LicenseStatementBuilder implements ILicenseInformationBuilder {
        @XmlElementRef(name = "licenseInfo", type = JAXBElement.class)
        private List<JAXBElement<? extends ILicenseInformationBuilder>> licenseInfo;
        @XmlAttribute(name = "op")
        private LicenseOperator op;

        public void setLicenseInfo(List<JAXBElement<? extends ILicenseInformationBuilder>> value) {
            this.licenseInfo = value;
        }

        public void setOp(LicenseOperator value) {
            this.op = value;
        }

        @Override
        public LicenseInformation build() {
            LicenseStatement license = new LicenseStatement();
            license.setLicenses(
                    licenseInfo.stream()
                            .map(l -> (ILicenseInformationBuilder) l.getValue())
                            .map(ILicenseInformationBuilder::build)
                            .collect(Collectors.toList())
            );
            license.setOp(op);
            return license;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LicenseBuilder implements ILicenseInformationBuilder {
        @XmlElements({
                @XmlElement(name = "id"),
                @XmlElement(name = "name")
        })
        private String id;
        @XmlElements({
                @XmlElement(name = "commonName"),
                @XmlElement(name = "longName")
        })
        private String commonName;
        private String text;
        private Map<String, String> properties = new HashMap<>();

        public void setId(String id) {
            this.id = id;
        }

        public void setCommonName(String commonName) {
            this.commonName = commonName;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }

        @Override
        public LicenseInformation build() {
            return new License(id, commonName, text, properties);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class WithLicenseBuilder implements ILicenseInformationBuilder {
        private LicenseBuilder license;
        private LicenseBuilder exception;

        public void setLicense(LicenseBuilder license) {
            this.license = license;
        }

        public void setException(LicenseBuilder exception) {
            this.exception = exception;
        }

        @Override
        public LicenseInformation build() {
            return new WithLicense((License) license.build(), (License) exception.build());
        }
    }
}
