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
package org.eclipse.sw360.antenna.model.license;

import java.util.Objects;
import java.util.function.Consumer;

public class WithLicense extends License implements LicenseInformation {
    private static final String SEPARATOR = " WITH ";
    private static final String TEXT_SEPARATOR = "with exception:";

    private static final String ERROR_MESSAGE_TEMPLATE = "Parameter %s is not separated properly with '%s'";

    private String exceptionId;
    private String exceptionName;
    private String exceptionText;

    public WithLicense() {
        this(null, null);
    }


    public WithLicense(String licenseId, String exceptionId) {
        this(licenseId, null, null, exceptionId, null, null);
    }

    public WithLicense(String licenseId, String licenseName, String licenseText, String exceptionId,
            String exceptionName, String exceptionText) {
        super(licenseId, licenseName, licenseText);
        this.exceptionId = exceptionId != null ? exceptionId : "";
        this.exceptionName = exceptionName != null ? exceptionName : "";
        this.exceptionText = exceptionText != null ? exceptionText : "";
    }

    public WithLicense(String licenseId, String licenseName, String licenseText) {
        super();
        exceptionId = separateStringAndSetFirst(licenseId, SEPARATOR, super::setId);
        exceptionName = separateStringAndSetFirst(licenseName, SEPARATOR, super::setCommonName);
        exceptionText = separateStringAndSetFirst(licenseText, TEXT_SEPARATOR, super::setText);
    }

    private String separateStringAndSetFirst(String source, String separator, Consumer<String> consumer) {
        String[] splitted = source.split(separator);
        if (splitted.length != 2) {
            throw new IllegalArgumentException(String.format(ERROR_MESSAGE_TEMPLATE, source, separator));
        }
        consumer.accept(splitted[0].trim());
        return splitted[1].trim();
    }

    @Override
    public String getId() {
        return evaluate();
    }

    @Override
    public void setId(String id) {
        exceptionId = separateStringAndSetFirst(id, SEPARATOR, super::setId);
    }

    @Override
    public String getCommonName() {
        if ("".equals(super.getCommonName()) || "".equals(exceptionName)) {
            return "";
        }
        return super.getCommonName() + SEPARATOR + exceptionName;
    }

    @Override
    public void setCommonName(String commonName) {
        exceptionName = separateStringAndSetFirst(commonName, SEPARATOR, super::setCommonName);
    }

    @Override
    public String getText() {
        if ("".equals(super.getText()) || "".equals(exceptionText)) {
            return "";
        }
        return super.getText() + "\n\n" + TEXT_SEPARATOR + "\n\n" + exceptionText;
    }

    @Override
    public void setText(String text) {
        exceptionText = separateStringAndSetFirst(text, TEXT_SEPARATOR, super::setText);
    }

    public String getLicenseId() {
        return super.getId();
    }

    public void setLicenseId(String licenseId) {
        super.setId(licenseId);
    }

    public String getLicenseName() {
        return super.getCommonName();
    }

    public void setLicenseName(String licenseName) {
        super.setCommonName(licenseName);
    }

    public String getLicenseText() {
        return super.getText();
    }
    public void setLicenseText(String licenseText) {
        super.setText(licenseText);
    }

    public String getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(String exceptionId) {
        this.exceptionId = exceptionId != null ? exceptionId : "";
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName != null ? exceptionName : "";
    }

    public String getExceptionText() {
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText != null ? exceptionText : "";
    }

    @Override
    public String evaluate() {
        if (isEmpty()) {
            return "";
        }
        return super.getId() + SEPARATOR + exceptionId;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || exceptionId.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        boolean parent = super.equals(o);
        if (parent && getClass().equals(o.getClass())) {
            WithLicense that = (WithLicense) o;
            return Objects.equals(exceptionId, that.exceptionId) &&
                    Objects.equals(exceptionName, that.exceptionName) &&
                    Objects.equals(exceptionText, that.exceptionText);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), exceptionId.hashCode(), exceptionName.hashCode(), exceptionText.hashCode());
    }
}
