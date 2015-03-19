/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.vaadin.oauth.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Named;
import net.anthavio.httl.HttlSender;
import net.anthavio.httl.auth.OAuth2;
import net.anthavio.httl.auth.OAuth2Builder;

@Named
@Stateless
public class OAuthUtil {

    List<OAuthProvider> oAuthProviders;

    public OAuthUtil() {
    }

    @PostConstruct
    private void init() {
        Properties properties = load("oauth.properties");
        String redirectUri = properties.getProperty("oauth.redirect_uri");
        oAuthProviders = new ArrayList<>();
        oAuthProviders.add(new OAuthProvider(OAuthProvider.OAuthProviderType.GOOGLE, buildGoogle(properties, redirectUri), "email"));
        oAuthProviders.add(new OAuthProvider(OAuthProvider.OAuthProviderType.FACEBOOK, buildFacebook(properties, redirectUri), "email"));
        //oAuthProviders.add(new OAuthProvider(OAuthProvider.OAuthProviderType.LINKEDIN, buildLinkedIn(properties, redirectUri), "r_basicprofile"));
        //oAuthProviders.add(new OAuthProvider(OAuthProvider.OAuthProviderType.GITHUB, buildGithub(properties, redirectUri), ""));
    }

    public OAuthProvider getByName(String provider) {
        for (OAuthProvider oAuthProvider : oAuthProviders) {
            if (oAuthProvider.getType().name().equalsIgnoreCase(provider))
                return oAuthProvider;
        }
        throw new IllegalArgumentException("No provider: " + provider );
    }

    private Properties load(String name) {
        String property = System.getProperty(name, name);

        try {
            InputStream stream;
            File file = new File(property);
            if (file.exists()) {
                stream = new FileInputStream(file);
            } else {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                stream = loader.getResourceAsStream(property);
                if (stream == null) {
                    throw new IllegalArgumentException("Properties resource not found: " + property);
                }
            }

            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        } catch (IOException iox) {
            throw new IllegalStateException("Properties resource " + property + " failed to load", iox);
        }
    }

    private OAuth2 buildGithub(Properties properties, String redirectUri) {
        HttlSender sender = HttlSender.url("https://github.com").httpClient4().sender()
                .addHeader("Accept", "application/json").build();

        String clientId = properties.getProperty("github.client_id");
        String clientSecret = properties.getProperty("github.client_secret");
        redirectUri = redirectUri.replace("{provider}", "github");

        OAuth2 oauth = new OAuth2Builder().setClientId(clientId).setClientSecret(clientSecret)
                .setTokenEndpoint(sender, "/login/oauth/access_token").setAuthorizationUrl("/login/oauth/authorize")
                .setRedirectUri(redirectUri).build();
        return oauth;
    }

    private OAuth2 buildGoogle(Properties properties, String redirectUri) {
        HttlSender sender = HttlSender.url("https://accounts.google.com").httpClient4().sender().build();

        String clientId = properties.getProperty("google.client_id");
        String clientSecret = properties.getProperty("google.client_secret");
        redirectUri = redirectUri.replace("{provider}", "google");

        OAuth2 oauth = new OAuth2Builder().setClientId(clientId).setClientSecret(clientSecret)
                .setTokenEndpoint(sender, "/o/oauth2/token").setAuthorizationUrl("/o/oauth2/auth").setRedirectUri(redirectUri)
                .build();
        return oauth;
    }

    private OAuth2 buildLinkedIn(Properties properties, String redirectUri) {
        HttlSender sender = HttlSender.url("https://www.linkedin.com").httpClient4().sender().build();

        String clientId = properties.getProperty("linkedin.client_id");
        String clientSecret = properties.getProperty("linkedin.client_secret");
        redirectUri = redirectUri.replace("{provider}", "linkedin");

        OAuth2 oauth = new OAuth2Builder().setClientId(clientId).setClientSecret(clientSecret)
                .setTokenEndpoint(sender, "/uas/oauth2/accessToken").setAuthorizationUrl("/uas/oauth2/authorization")
                .setRedirectUri(redirectUri).build();
        return oauth;
    }

    /**
     * https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow/v2.1
     */
    private OAuth2 buildFacebook(Properties properties, String redirectUri) {
        HttlSender sender = HttlSender.url("https://graph.facebook.com").httpClient4().sender().build();

        String clientId = properties.getProperty("facebook.client_id");
        String clientSecret = properties.getProperty("facebook.client_secret");
        redirectUri = redirectUri.replace("{provider}", "facebook");

        OAuth2 oauth = new OAuth2Builder().setClientId(clientId).setClientSecret(clientSecret)
                .setAuthParam("display", "popup").setTokenEndpoint(sender, "/oauth/access_token")
                .setAuthorizationUrl("https://www.facebook.com/dialog/oauth").setRedirectUri(redirectUri).build();
        return oauth;
    }

}
