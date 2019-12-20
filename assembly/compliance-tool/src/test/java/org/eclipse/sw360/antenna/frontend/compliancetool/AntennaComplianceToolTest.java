package org.eclipse.sw360.antenna.frontend.compliancetool;

import org.eclipse.sw360.antenna.frontend.compliancetool.main.AntennaComplianceTool;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Objects;

public class AntennaComplianceToolTest {

    @Ignore
    @Test
    public void testMainWithExporter() {
        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-exporter.properties")).getPath();
        String[] args = new String[]{ "exporter", propertiesFilePath };
        AntennaComplianceTool.main(args);
    }

}
