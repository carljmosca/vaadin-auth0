/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.vaadin.oauth.util;

import java.util.Arrays;
import net.anthavio.httl.auth.OAuth2;

/**
 *
 * @author moscac
 */
public class OAuthProvider {

    public static enum OAuthProviderType {

        GOOGLE, FACEBOOK, GITHUB, LINKEDIN;

    }

    private OAuth2 oauth;

    private String scopes;
    
    private OAuthProviderType type;
    
    public OAuthProvider(OAuthProviderType type, OAuth2 oauth, String scopes) {
        this.type = type;
        setOAuth(oauth, scopes);
    }

    public static OAuthProviderType getByName(String provider) {
        provider = provider.toLowerCase();
        OAuthProviderType[] values = OAuthProviderType.values();
        for (OAuthProviderType value : values) {
            if (value.name().toLowerCase().equals(provider)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No provider: " + provider + " Available: " + Arrays.asList(values));
    }

    public OAuth2 getOAuth() {
        return oauth;
    }

    public String getScopes() {
        return scopes;
    }

    public OAuthProviderType getType() {
        return type;
    }

    private void setOAuth(OAuth2 oauth, String scopes) {
        if (oauth == null) {
            throw new IllegalArgumentException("Null OAuth2");
        }
        this.oauth = oauth;
        if (scopes == null) {
            throw new IllegalArgumentException("Null scopes");
        }
        this.scopes = scopes;
    }
}
