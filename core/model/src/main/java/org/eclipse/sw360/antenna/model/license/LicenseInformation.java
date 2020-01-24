package org.eclipse.sw360.antenna.model.license;


import java.util.List;

public interface LicenseInformation {
    String evaluate();
    String evaluateLong();
    boolean isEmpty();
    List<License> getLicenses();
}
