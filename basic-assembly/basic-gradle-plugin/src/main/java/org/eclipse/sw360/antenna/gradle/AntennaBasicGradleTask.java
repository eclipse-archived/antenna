package org.eclipse.sw360.antenna.gradle;

import org.eclipse.sw360.antenna.frontend.gradle.AnalyzeTask;

public class AntennaBasicGradleTask extends AnalyzeTask {
    protected String getPluginDescendantArtifactIdName() {
        return "basic-maven-plugin";
    }
}
