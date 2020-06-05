package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

abstract public class InfoRequest<T> {
    abstract public String getInfoParameter();

    boolean hasAdditionalParameters() {
        return getAdditionalParameters().size() > 0;
    }

    abstract String helpMessage();
    abstract boolean isValid();
    abstract Set<String> getAdditionalParameters();
    abstract void parseAdditionalParameter(Set<String> parameters);
    abstract Collection<T> execute(SW360Connection connection);

    static InfoRequest emptyInfoRequest() {
        return new InfoRequest<Object>() {
            @Override
            public String getInfoParameter() {
                return "NON_VALID";
            }

            @Override
            String helpMessage() {
                return "The provided info parameter is not supported in this reporter reporter";
            }

            @Override
            boolean isValid() {
                return false;
            }

            @Override
            Set<String> getAdditionalParameters() {
                return Collections.emptySet();
            }

            @Override
            void parseAdditionalParameter(Set<String> parameters) {
                // no-op
            }

            @Override
            Collection<Object> execute(SW360Connection connection) {
                return Collections.emptySet();
            }
        };
    }
}
