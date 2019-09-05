package org.eclipse.sw360.antenna.model.license;

public class EmptyLicenseInformation extends LicenseInformation {
    @Override
    public LicenseInformation and(LicenseInformation other) {
        return other;
    }

    @Override
    public LicenseInformation or(LicenseInformation other) {
        return other;
    }

    @Override
    public String toSpdxExpression() {
        return "";
    }
}
