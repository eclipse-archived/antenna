package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.stub.cli.AbstractAntennaCLIOptions;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter.ReporterParameterParser.DEFAULT_OUTPUT_FORMAT;
import static org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter.ReporterParameterParser.REPORTER_PARAMETER_PREFIX;

public class ReporterParameterParserTest {

    private final String id = "--id";


    @Test(expected = NullPointerException.class)
    public void checkParametersNullParameters() {
        ReporterParameterParser.checkParameters(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkParametersEmptyParameters() {
        ReporterParameterParser.checkParameters(
                Collections.emptySet());
    }

    @Test(expected = IllegalStateException.class)
    public void mapParametersFromMultipleInfoParameters() {
        ReporterParameterParser.mapParameters(new HashSet<>(Arrays.asList(REPORTER_PARAMETER_PREFIX + "=first", REPORTER_PARAMETER_PREFIX + "=second")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mapParametersFromParameterWithoutValueAfterParameterIdentifier() {
        Set<String> parameters = Collections.singleton(id + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER);

        ReporterParameterParser.mapParameters(parameters);

        fail("Did not throw expected exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mapParametersFromParameterWithTwoParameterIdentifier() {
        Set<String> parameters = Collections.singleton(id + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + "mockValue" + AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER + "mockValue2");

        ReporterParameterParser.mapParameters(parameters);

        fail("Did not throw expected exception");
    }

    @Test
    public void getInfoParameterFromParameter() {
        String infoParameter = "infoParameter";
        String additionalParameter_Id = "id";
        final String infoParameterFromParameter = ReporterParameterParser.getInfoParameterFromParameters(
                new HashMap<String, String>() {
                    {
                        put(REPORTER_PARAMETER_PREFIX, infoParameter);
                        put(additionalParameter_Id, "12345");
                    }

                }
        );

        assertThat(infoParameterFromParameter).isEqualTo(infoParameter);
    }


    @Test
    public void parseParameterValueFromListOfParameterWithContainedExactParameter() {
        final String idValue = "12345";
        Map<String, String> parameters = new HashMap<String, String>() {
            {
                put(REPORTER_PARAMETER_PREFIX, "info-parameter");
                put(id, idValue);
                put("--version", "1.0.0");
            }

        };
        String parameterValue = ReporterParameterParser.parseParameterValueFromMapOfParameters(parameters, id);

        assertThat(parameterValue).isEqualTo(idValue);
    }

    @Test
    public void parseParameterValueFromListOfParameterWithoutContainedParameter() {
        final String idValue = "12345";
        final String idPlus = id + "plus";
        Map<String, String> parameters = new HashMap<String, String>() {
            {
                put(REPORTER_PARAMETER_PREFIX, "info-parameter");
                put(idPlus, idValue);
                put("--version", "1.0.0");
            }

        };
        String parameterValue = ReporterParameterParser.parseParameterValueFromMapOfParameters(parameters, id);

        assertThat(parameterValue).isEqualTo(null);
    }

    @Test
    public void getOutputFormatFromShortSwitch() {
        String outputFormat = "CSV";
        Map<String, String> parameters = Collections.singletonMap(
                ReporterParameterParser.OUTPUT_FORMAT_PREFIX_SHORT, outputFormat);
        final String gottenOutputFormat = ReporterParameterParser.getOutputFormat(parameters);

        assertThat(gottenOutputFormat).isEqualTo(outputFormat);
    }

    @Test
    public void getOutputFormatFromLongSwitch() {
        String outputFormat = "CSV";
        Map<String, String> parameters = Collections.singletonMap(
                ReporterParameterParser.OUTPUT_FORMAT_PREFIX_LONG, outputFormat);
        final String gottenOutputFormat = ReporterParameterParser.getOutputFormat(parameters);

        assertThat(gottenOutputFormat).isEqualTo(outputFormat);
    }

    @Test
    public void getOutputFormatMissingParameter() {
        String wrongParameterPrefix = "--some-parameter=";
        String outputFormat = "something";
        Map<String, String> parameters = Collections.singletonMap(wrongParameterPrefix, outputFormat);
        final String setOutputFormat = ReporterParameterParser.getOutputFormat(parameters);

        assertThat(setOutputFormat).isEqualTo(DEFAULT_OUTPUT_FORMAT);
    }
}
