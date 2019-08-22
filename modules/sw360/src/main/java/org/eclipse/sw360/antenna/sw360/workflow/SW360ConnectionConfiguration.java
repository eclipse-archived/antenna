package org.eclipse.sw360.antenna.sw360.workflow;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.SW360AuthenticationClient;
import org.springframework.http.HttpHeaders;

public class SW360ConnectionConfiguration {
    public static final String REST_SERVER_URL_KEY = "rest.server.url";
    public static final String AUTH_SERVER_URL_KEY = "auth.server.url";
    public static final String USERNAME_KEY = "user.id";
    public static final String PASSWORD_KEY = "user.password";
    public static final String CLIENT_USER_KEY = "client.id";
    public static final String CLIENT_PASSWORD_KEY = "client.password";
    public static final String PROXY_USE = "proxy.use";

    private final String restServerUrl;
    private final String authServerUrl;
    private final String user;
    private final String password;
    private final String clientId;
    private final String clientPassword;
    private final String proxyHost;
    private final int proxyPort;
    private final boolean proxyUse;
    private final SW360AuthenticationClient authenticationClient;

    public SW360ConnectionConfiguration(Getter<String> getConfigValue, Getter<Boolean> getBooleanConfigValue, String proxyHost, int proxyPort) throws AntennaConfigurationException {
        // SW360 Connection configuration
        restServerUrl = getConfigValue.apply(SW360ConnectionConfiguration.REST_SERVER_URL_KEY);
        authServerUrl = getConfigValue.apply(SW360ConnectionConfiguration.AUTH_SERVER_URL_KEY);
        user = getConfigValue.apply(SW360ConnectionConfiguration.USERNAME_KEY);
        password = getConfigValue.apply(SW360ConnectionConfiguration.PASSWORD_KEY);
        clientId = getConfigValue.apply(SW360ConnectionConfiguration.CLIENT_USER_KEY);
        clientPassword = getConfigValue.apply(SW360ConnectionConfiguration.CLIENT_PASSWORD_KEY);

        // Proxy configuration
        proxyUse = getBooleanConfigValue.apply(SW360ConnectionConfiguration.PROXY_USE);

        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;

        this.authenticationClient = getSW360AuthenticationClient();
    }

    public SW360ConnectionConfiguration(String restServerUrl, String authServerUrl, String user, String password, String clientId, String clientPassword, String proxyHost, int proxyPort, boolean proxyUse) {
        this.restServerUrl = restServerUrl;
        this.authServerUrl = authServerUrl;
        this.user = user;
        this.password = password;
        this.clientId = clientId;
        this.clientPassword = clientPassword;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUse = proxyUse;

        this.authenticationClient = getSW360AuthenticationClient();
    }

    public SW360AuthenticationClient getSW360AuthenticationClient() {
        return new SW360AuthenticationClient(authServerUrl, proxyUse, proxyHost, proxyPort);
    }

    public SW360ComponentClientAdapter getSW360ComponentClientAdapter() {
        return new SW360ComponentClientAdapter(restServerUrl, proxyUse, proxyHost, proxyPort);
    }

    public SW360ReleaseClientAdapter getSW360ReleaseClientAdapter() {
        return new SW360ReleaseClientAdapter(restServerUrl, proxyUse, proxyHost, proxyPort);
    }

    public SW360LicenseClientAdapter getSW360LicenseClientAdapter() {
        return new SW360LicenseClientAdapter(restServerUrl, proxyUse, proxyHost, proxyPort);
    }

    public SW360ProjectClientAdapter getSW360ProjectClientAdapter() {
        return new SW360ProjectClientAdapter(restServerUrl, proxyUse, proxyHost, proxyPort);
    }

    public HttpHeaders getHttpHeaders() throws AntennaException {
        return authenticationClient.getHeadersWithBearerToken(authenticationClient.getOAuth2AccessToken(user, password, clientId, clientPassword));
    }

    @FunctionalInterface
    public interface Getter<T> {
        T apply(String s) throws AntennaConfigurationException;
    }
}
