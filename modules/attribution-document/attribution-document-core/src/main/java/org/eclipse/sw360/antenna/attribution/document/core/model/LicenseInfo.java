package org.eclipse.sw360.antenna.attribution.document.core.model;

import org.apache.commons.lang3.Validate;

public class LicenseInfo {
    private final String key, title, text, shortName;

    /**
     * @param key       (non-null) A key that identifies the license and that can be used as anchor in PDFBox
     * @param text      (non-null) The license text.
     * @param shortName (non-null) the shortname of the license.
     * @param title     (nullable) The title. If not set, the shortName will be used instead.
     */
    public LicenseInfo(String key, String text, String shortName, String title) {
        Validate.isTrue(key != null, "Key must not be null");
        Validate.isTrue(shortName != null, "Shortname must not be null");
        Validate.isTrue(text != null, "License text must nut be null");
        this.key = key;
        this.shortName = shortName;
        this.title = title;
        this.text = text;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        if (title == null) {
            return getShortName();
        }
        return title;
    }

    public String getText() {
        return text;
    }

    public String getShortName() {
        return shortName;
    }
}
