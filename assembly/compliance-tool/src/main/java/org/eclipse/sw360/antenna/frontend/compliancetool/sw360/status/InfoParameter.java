package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;

import java.util.Set;

abstract public class InfoParameter {
    Set<String> parameters;
    abstract public String getInfoParameter();
    abstract boolean hasAdditionalParameters();
    abstract String helpMessage();
    abstract boolean isValid();
    abstract Set<String> getAdditionalParameters();
    abstract void parseAdditionalParameter(Set<String> parameters);
    abstract Object execute(SW360Connection connection);

    static InfoParameter emptyInfoParameter() {
        return new InfoParameter() {
            @Override
            public String getInfoParameter() {
                return "NON_VALID";
            }

            @Override
            boolean hasAdditionalParameters() {
                return false;
            }

            @Override
            String helpMessage() {
                return "The provided info parameter is not supported in this status reporter";
            }

            @Override
            boolean isValid() {
                return false;
            }

            @Override
            Set<String> getAdditionalParameters() {
                return null;
            }

            @Override
            void parseAdditionalParameter(Set<String> parameters) {
                // no-op
            }

            @Override
            Object execute(SW360Connection connection) {
                return null;
            }
        };
    }
}
