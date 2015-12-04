package org.ometa.lovemonster.models;

/**
 * Represents an Okta server to authenticate against.
 */
public class OktaServer {

    /**
     * The okta login page url.
     */
    public String loginUrl;

    /**
     * The callback for successful login.
     */
    public String loginSuccessCallbackUrl;

    /**
     * Singleton instance for the okta server.
     */
    private static final OktaServer singleton = new OktaServer();


    /**
     * Returns the singleton {@code OktaServer}, which contains settings for interacting with an Okta
     * Server.
     *
     * @return
     *      the singleton okta server
     */
    public static OktaServer getInstance() {
        return singleton;
    }

    protected OktaServer() {
        this.loginUrl = "https://groupon.okta.com/";
        this.loginSuccessCallbackUrl = "/app/UserHome";
    }
}
