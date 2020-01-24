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
        public abstract String evaluateLong();
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
        private String longName;
        private String text;
        private Map<String, String> properties = new HashMap<>();

        public void setId(String id) {
            this.id = id;
        }

        public void setLongName(String longName) {
            this.longName = longName;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }

        @Override
        public LicenseInformation build() {
            return new License(id, longName, text, properties);
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
