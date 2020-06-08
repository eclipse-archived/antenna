package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Central interface for the report output of the status reporter
 */
public interface ReporterOutput {
    void setResultType(Class type);
    void setFilePath(Path filePath);
    <T> void print(Collection<T> result);
}
