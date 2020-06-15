/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *     An interface that represents an information request
 *     made to the status reporter. This is meant to give
 *     needed information on objects in an SW360 instance.
 *     What kind of information can be determined by the
 *     execute() function.
 * </p>
 * <p>
 *     It is possible to give a class impelenting this interface
 *     additional parameters which can be set to set values of
 *     parameters.
 * </p>
 * @param <T>
 */
public interface InfoRequest<T> {
    String getInfoParameter();

    default boolean hasAdditionalParameters() {
        return !getAdditionalParameters().isEmpty();
    }

    /**
     * Per default an implementing class will have an empty
     * set of parameters, unless it chooses to set some.
     *
     * @return Set of all names of the additional parameters,
     * which by default is empty.
     */
    default Set<String> getAdditionalParameters() {
        return Collections.emptySet();
    }

    /**
     * Per default an implementing class will have an empty
     * set of parameters, unless it chooses to set some.
     * Hence parsing the additional parameters is by default
     * a no-op.
     * Otherwise this method can be used to map the parameter
     * values on their corresponding variables
     *
     * @param parameters map of parameters for parsing.
     */
    default void parseAdditionalParameter(Map<String, String> parameters) {
        //no -op
    }

    /**
     * Gives a help message for the use of this parameters.
     * Ideally, it should contain all additional parameters
     *
     * @return Help message
     */
    String helpMessage();

    /**
     * Represents if the info request has all information it
     * needs to successfully run the execute() method.
     * @return if true, all information is present to run
     * the execute function, otherwise false
     */
    boolean isValid();

    /**
     * Tries to retrieve the date from a given SW360Connection
     * this info request is made for.
     *
     * @param connection SW360Connection supplying the information
     *                   for a working SW360 instance
     * @return collection of sw360 objects that were retrieved
     */
    Collection<T> execute(SW360Connection connection);

    /**
     * This gives the information of the kind of class that
     * is returned by the Collection of the execute function
     *
     * @return class of the generic of the interface
     */
    Class<T> getType();

    static InfoRequest<Object> emptyInfoRequest() {
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
