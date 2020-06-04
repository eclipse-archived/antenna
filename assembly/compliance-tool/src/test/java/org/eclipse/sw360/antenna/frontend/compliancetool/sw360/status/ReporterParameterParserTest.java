package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import org.eclipse.sw360.antenna.frontend.stub.cli.AbstractAntennaCLIOptions;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status.ReporterParameterParser.REPORTER_PARAMETER_PREFIX;

public class ReporterParameterParserTest {

    private final String id = "--id";

    @Test(expected = IllegalArgumentException.class)
    public void getInfoParameterFromEmptyParameters() {
        ReporterParameterParser.getInfoParameterFromParameters(
                Collections.emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getInfoParameterFromMultipleInfoParameters() {
        ReporterParameterParser.getInfoParameterFromParameters(
                new HashSet<>(Arrays.asList(REPORTER_PARAMETER_PREFIX + "=first", REPORTER_PARAMETER_PREFIX + "=second")));
    }

    @Test
    public void getInfoParameterFromParameter() {
        String infoParameter = new IRGetReleasesOfProjects().getInfoParameter();
        String additionalParameter_projectId = new IRGetReleasesOfProjects().getAdditionalParameters()
                .stream()
                .filter(s -> s.contains("id"))
                .findFirst()
                .get();
        final String infoParameterFromParameter = ReporterParameterParser.getInfoParameterFromParameters(
                new HashSet<>(Arrays.asList(new IRGetReleasesOfProjects().getInfoParameter(), additionalParameter_projectId + "=12345"))
        );

        assertThat(infoParameterFromParameter).isEqualTo(infoParameter);
    }


    @Test
    public void parseParameterValueFromListOfParameterWithContainedExactParameter() {
        final String idValue = "12345";
        Set<String> parameters = new HashSet<>(Arrays.asList("--info=info-parameter", id + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + idValue, "--version=1.0.0"));

        String parameterValue = ReporterParameterParser.parseParameterValueFromListOfParameters(parameters, id);

        assertThat(parameterValue).isEqualTo(idValue);
    }

    @Test
    public void parseParameterValueFromListOfParameterWithoutContainedParameter() {
        final String idValue = "12345";
        final String idPlus = id + "plus";
        Set<String> parameters = new HashSet<>(Arrays.asList("--info=info-parameter", idPlus + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + idValue, "--version=1.0.0"));

        String parameterValue = ReporterParameterParser.parseParameterValueFromListOfParameters(parameters, id);

        assertThat(parameterValue).isEqualTo(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseParameterValueFromListOfParameterFailsWithoutValueAfterParameterIdentifier() {
        Set<String> parameters = new HashSet<>(Arrays.asList("--info=info-parameter", id + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER, "--version=1.0.0"));

        ReporterParameterParser.parseParameterValueFromListOfParameters(parameters, id);

        fail("Did not throw excpected exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseParameterValueFromListOfParameterFailsWithTwoParameterIdentifier() {
        Set<String> parameters = new HashSet<>(Arrays.asList("--info=info-parameter", id + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + "mockValue" + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + "mockValue2", "--version=1.0.0"));

        ReporterParameterParser.parseParameterValueFromListOfParameters(parameters, id);

        fail("Did not throw excpected exception");
    }

    @Test
    public void getOutputFormatFromShortSwitch() {
        String outputFormat = "CSV";
        Set<String> parameters = Collections.singleton(ReporterParameterParser.OUTPUT_FORMAT_PREFIX_SHORT+  AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + outputFormat);
        final String gottenOutputFormat = ReporterParameterParser.getOutputFormat(parameters);

        assertThat(gottenOutputFormat).isEqualTo(outputFormat);
    }

    @Test
    public void getOutputFormatFromLongSwitch() {
        String outputFormat = "CSV";
        Set<String> parameters = Collections.singleton(ReporterParameterParser.OUTPUT_FORMAT_PREFIX_LONG + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + outputFormat);
        final String gottenOutputFormat = ReporterParameterParser.getOutputFormat(parameters);

        assertThat(gottenOutputFormat).isEqualTo(outputFormat);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOutputFormatMissingParameter() {
        String wrongParameterPrefix = "--some-parameter=";
        String outputFormat = "CSV";
        Set<String> parameters = Collections.singleton(wrongParameterPrefix + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + outputFormat);
        ReporterParameterParser.getOutputFormat(parameters);

        fail("Should have failed due to missing output format");
    }
}