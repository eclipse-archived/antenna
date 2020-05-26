package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import java.util.Set;

abstract public class InfoParameter {
    Set<String> parameters;
    abstract public String getInfoParameter();
    abstract boolean hasAdditionalParameters();
    abstract String helpMessage();
    abstract boolean isValid();
    abstract Set<String> getAdditionalParameters();
    abstract void execute();
}
