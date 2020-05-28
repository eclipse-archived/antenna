package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;

import java.util.Collection;

/**
 * Central interface for the report output of the status reporter
 * @param <T>
 */
public interface ReporterOutput<T> {
    void printFile(Collection<T> result, SW360Configuration configuration);
}
