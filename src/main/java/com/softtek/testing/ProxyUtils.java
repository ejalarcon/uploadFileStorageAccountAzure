package com.softtek.testing;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyUtils {

    private static Logger log = LoggerFactory.getLogger(ProxyUtils.class);

    public static void initializeProxyAuthenticator() {
	final String proxyUser = System.getProperty("https.proxyUser");
	final String proxyPassword = System.getProperty("https.proxyPassword");

	log.debug("Proxy: URL=" + System.getProperty("https.proxyHost") + ":" + System.getProperty("https.proxyPort"));

	if (proxyUser != null && proxyPassword != null) {
	    Authenticator.setDefault(new Authenticator() {
		public PasswordAuthentication getPasswordAuthentication() {
		    return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
		}
	    });
	}
    }
}
