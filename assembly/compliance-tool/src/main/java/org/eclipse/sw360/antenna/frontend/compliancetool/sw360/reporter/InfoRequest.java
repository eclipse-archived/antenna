package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface InfoRequest<T> {
    String getInfoParameter();

    default boolean hasAdditionalParameters() {
        return getAdditionalParameters().size() > 0;
    }

    default Set<String> getAdditionalParameters() {
        return Collections.emptySet();
    }

    default void parseAdditionalParameter(Map<String, String> parameters) {
        //no -op
    }

    String helpMessage();
    boolean isValid();
    Collection<T> execute(SW360Connection connection);
    Class<T> getType();

    static InfoRequest emptyInfoRequest() {
        return new InfoRequest<Object>() {
            @Override
            public String getInfoParameter() {
                return "NON_VALID";
            }

            @Override
            public String helpMessage() {
                return "The provided info parameter is not supported in this reporter reporter";
            }

            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public void parseAdditionalParameter(Map<String, String> parameters) {
                // no-op
            }

            @Override
            public Collection<Object> execute(SW360Connection connection) {
                return Collections.emptySet();
            }

            @Override
            public Class<Object> getType() {
                return Object.class;
            }
        };
    }
}
