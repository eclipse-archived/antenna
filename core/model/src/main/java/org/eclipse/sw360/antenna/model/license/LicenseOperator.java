package org.eclipse.sw360.antenna.model.license;

public enum LicenseOperator {
    AND,
    OR;

    public static LicenseOperator fromValue(String operator) {
        return valueOf(operator.toUpperCase());
    }
}
