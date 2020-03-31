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

/**
 * This class reflects a with license, i.e., a classical Open Source license with an exception. A widely known example
 * is the 'GPL-2.0-or-later WITH Classpath-exception-2.0'. The class is setup to handle both parts of the licenses
 * separately, the main license by using the inherited license properties, the exception information internally.
 * It can handle input as separate parts for the two parts or as combined expressions. The underlying assumption is
 * that a with license is expressed as 'main_license_id WITH exception_id'. The case of the WITH statement matters, so
 * only upper case WITH is allowed. For the license texts the separator is 'with exception:' with embedding whitespaces.
 */
public class WithLicense extends License implements LicenseInformation {
    private static final String SEPARATOR = " WITH ";
    private static final String TEXT_SEPARATOR = "with exception:";

    private static final String ERROR_MESSAGE_TEMPLATE = "Parameter %s is not separated properly with '%s'";

    private String exceptionId;
    private String exceptionName;
    private String exceptionText;

    /**
     * Creates an empty license
     */
    public WithLicense() {
        this(null, null);
    }

    /**
     * Constructor to create a license with only license ids. This is enough to identify a license, within the
     * workflow. Corresponding to this, the {@link #equalLicense(License)} method checks for equality of licenses
     * only based on the ids.
     *
     * @param licenseId The id of the main license in the with expression, must not be null.
     * @param exceptionId The id of the exception, must not be null.
     */
    public WithLicense(String licenseId, String exceptionId) {
        this(licenseId, null, null, exceptionId, null, null);
    }

    /**
     * Constructor for a full license, including ids, names and license texts.
     *
     * @param licenseId The id of the main license, normally a SPDX identifier, must not be null.
     * @param licenseName The name of the main license in a more verbal form, must not be null.
     * @param licenseText The license text of the main license as a formatted text string, must not be null.
     * @param exceptionId The id of the exception, normally a SPDX identifier, must not be null.
     * @param exceptionName The name of the exception in a more verbal form, must not be null.
     * @param exceptionText The license text of the exception as a formatted text string, must not be null.
     */
    public WithLicense(String licenseId, String licenseName, String licenseText, String exceptionId,
            String exceptionName, String exceptionText) {
        super(licenseId, licenseName, licenseText);
        this.exceptionId = exceptionId != null ? exceptionId : "";
        this.exceptionName = exceptionName != null ? exceptionName : "";
        this.exceptionText = exceptionText != null ? exceptionText : "";
    }

    /**
     * Constructor for combined expressions, i.e., for a with license that is already build together according to
     * the conventions above.
     *
     * @param licenseId A with license id, e.g., 'mainId WITH exceptionId', must not be null.
     * @param licenseName A with license name, e.g., 'mainName WITH exceptionName', must not be null.
     * @param licenseText A with license text, e.g,, 'mainText with exception: exceptionText', must not be null.
     * @throws IllegalArgumentException If the given parameters cannot be splitted according to the spec.
     */
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

    /**
     * @return The with license id, i.e., 'licenseId WITH exceptionId'.
     */
    @Override
    public String getId() {
        return evaluate();
    }

    /**
     * Set the with license id
     *
     * @param id A with license id, e.g., 'mainId WITH exceptionId', must not be null.
     * @throws IllegalArgumentException If the given parameters cannot be splitted according to the spec.
     */
    @Override
    public void setId(String id) {
        exceptionId = separateStringAndSetFirst(id, SEPARATOR, super::setId);
    }

    /**
     * @return The with license name, i.e., 'mainName WITH exceptionName'.
     */
    @Override
    public String getCommonName() {
        if ("".equals(super.getCommonName()) || "".equals(exceptionName)) {
            return "";
        }
        return super.getCommonName() + SEPARATOR + exceptionName;
    }

    /**
     * Set the with license name.
     *
     * @param commonName A with license name, e.g., 'mainName WITH exceptionName', must not be null.
     * @throws IllegalArgumentException If the given parameters cannot be splitted according to the spec.
     */
    @Override
    public void setCommonName(String commonName) {
        exceptionName = separateStringAndSetFirst(commonName, SEPARATOR, super::setCommonName);
    }

    /**
     * @return The with license text, i.e., 'mainText with exception: exceptionText'.
     */
    @Override
    public String getText() {
        if ("".equals(super.getText()) || "".equals(exceptionText)) {
            return "";
        }
        return super.getText() + "\n\n" + TEXT_SEPARATOR + "\n\n" + exceptionText;
    }

    /**
     * Set the with license text.
     *
     * @param text A with license text, e.g,, 'mainText with exception: exceptionText', must not be null.
     * @throws IllegalArgumentException If the given parameters cannot be splitted according to the spec.
     */
    @Override
    public void setText(String text) {
        exceptionText = separateStringAndSetFirst(text, TEXT_SEPARATOR, super::setText);
    }

    /**
     * @return The id of the main license
     */
    public String getLicenseId() {
        return super.getId();
    }

    /**
     * Set the main license id.
     *
     * @param licenseId The id of the main license, normally a SPDX identifier, must not be null.
     */
    public void setLicenseId(String licenseId) {
        super.setId(licenseId);
    }

    /**
     * @return The name of the main license
     */
    public String getLicenseName() {
        return super.getCommonName();
    }

    /**
     * Set the main license name.
     *
     * @param licenseName The name of the main license in a more verbal form, must not be null.
     */
    public void setLicenseName(String licenseName) {
        super.setCommonName(licenseName);
    }

    /**
     * @return The text of the main license
     */
    public String getLicenseText() {
        return super.getText();
    }

    /**
     * Set the main license text.
     *
     * @param licenseText The license text of the main license as a formatted text string, must not be null.
     */
    public void setLicenseText(String licenseText) {
        super.setText(licenseText);
    }

    /**
     * @return The id of the exception
     */
    public String getExceptionId() {
        return exceptionId;
    }

    /**
     * Set the exception id.
     *
     * @param exceptionId The id of the exception, normally a SPDX identifier, must not be null.
     */
    public void setExceptionId(String exceptionId) {
        this.exceptionId = exceptionId != null ? exceptionId : "";
    }

    /**
     * @return The name of the exception
     */
    public String getExceptionName() {
        return exceptionName;
    }

    /**
     * Set the exception name.
     *
     * @param exceptionName The name of the exception in a more verbal form, must not be null.
     */
    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName != null ? exceptionName : "";
    }

    /**
     * @return The text of the exception
     */
    public String getExceptionText() {
        return exceptionText;
    }

    /**
     * Set the exception text.
     *
     * @param exceptionText The license text of the exception as a formatted text string, must not be null.
     */
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
