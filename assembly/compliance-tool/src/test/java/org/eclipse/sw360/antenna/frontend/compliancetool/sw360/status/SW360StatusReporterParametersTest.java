package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import org.eclipse.sw360.antenna.frontend.stub.cli.AbstractAntennaCLIOptions;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status.SW360StatusReporterParameters.REPORTER_PARAMETER_PREFIX;

public class SW360StatusReporterParametersTest {
    @Test(expected = IllegalArgumentException.class)
    public void getInfoRequestFromEmptyParameters() {
        SW360StatusReporterParameters.getInfoRequestFromParameter(
                Collections.emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getInfoRequestFromMultipleInfoParameters() {
        SW360StatusReporterParameters.getInfoRequestFromParameter(
                new HashSet<>(Arrays.asList(REPORTER_PARAMETER_PREFIX + "=first", REPORTER_PARAMETER_PREFIX + "=second")));
    }


    @Test
    public void parseParameterValueFromListOfParameterWithContainedExactParameter() {
        final String idValue = "12345";
        final String id = "--id";
        Set<String> parameters = new HashSet<>(Arrays.asList("--info=info-parameter",id + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + idValue, "--version=1.0.0"));

        String parameterValue = SW360StatusReporterParameters.parseParameterValueFromListOfParameters(parameters, id);

        assertThat(parameterValue).isEqualTo(idValue);
    }

    @Test
    public void parseParameterValueFromListOfParameterWithoutContainedParameter() {
        final String idValue = "12345";
        final String id = "--id";
        final String idPlus = id + "plus";
        Set<String> parameters = new HashSet<>(Arrays.asList("--info=info-parameter",idPlus + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + idValue, "--version=1.0.0"));

        String parameterValue = SW360StatusReporterParameters.parseParameterValueFromListOfParameters(parameters, id);

        assertThat(parameterValue).isEqualTo(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseParameterValueFromListOfParameterFailsWithoutValueAfterParameterIdentifier() {
        final String id = "--id";
        Set<String> parameters = new HashSet<>(Arrays.asList("--info=info-parameter",id + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER, "--version=1.0.0"));

        SW360StatusReporterParameters.parseParameterValueFromListOfParameters(parameters, id);

        fail("Did not throw excpected exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseParameterValueFromListOfParameterFailsWithTwoParameterIdentifier() {
        final String id = "--id";
        Set<String> parameters = new HashSet<>(Arrays.asList("--info=info-parameter",id + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + "mockValue" + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + "mockValue2", "--version=1.0.0"));

        SW360StatusReporterParameters.parseParameterValueFromListOfParameters(parameters, id);

        fail("Did not throw excpected exception");
    }
}