package org.eclipse.sw360.antenna.sw360.utils;

public class SW360ClientException extends RuntimeException {

    public SW360ClientException() {
    }

    public SW360ClientException(String s) {
        super(s);
    }

    public SW360ClientException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SW360ClientException(Throwable throwable) {
        super(throwable);
    }
}
