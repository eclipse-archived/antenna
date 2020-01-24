package org.eclipse.sw360.antenna.model.license;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WithLicense implements LicenseInformation {
    private License license;
    private License exception;

    public WithLicense() {
        this(new License(), new License());
    }

    public WithLicense(License license, License exception) {
        this.license = license;
        this.exception = exception;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public License getException() {
        return exception;
    }

    public void setException(License exception) {
        this.exception = exception;
    }

    @Override
    public String evaluate() {
        return license.getName() + " WITH " + exception.getName();
    }

    @Override
    public String evaluateLong() {
        return license.getLongName() + " WITH " + exception.getLongName();
    }

    @Override
    public boolean isEmpty() {
        return this.license.isEmpty() && this.exception.isEmpty();
    }

    @Override
    public List<License> getLicenses() {
        return Stream.of(license, exception).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WithLicense that = (WithLicense) o;
        return Objects.equals(license, that.license) &&
                Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(license, exception);
    }

    @Override
    public String toString() {
        return evaluate();
    }
}
