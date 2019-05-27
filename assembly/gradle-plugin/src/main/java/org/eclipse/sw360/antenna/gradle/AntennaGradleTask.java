package org.eclipse.sw360.antenna.gradle;

import org.eclipse.sw360.antenna.frontend.gradle.AnalyzeTask;

public class AntennaGradleTask extends AnalyzeTask {
    protected String getPluginDescendantArtifactIdName() {
        return "antenna-maven-plugin";
    }
}
