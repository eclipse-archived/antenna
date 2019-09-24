package org.eclipse.sw360.antenna.model.license;

public class EmptyLicenseInformation extends LicenseInformation {
    @Override
    public LicenseInformation and(LicenseInformation other) {
        return new LicenseStatement(other);
    }

    @Override
    public LicenseInformation or(LicenseInformation other) {
        return new LicenseStatement(other);
    }

    @Override
    public String toSpdxExpression() {
        return "";
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
