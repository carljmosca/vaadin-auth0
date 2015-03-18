package com.github.vaadin.oauth;

import com.github.vaadin.oauth.util.OAuthProvider;
import com.github.vaadin.oauth.util.OAuthProvider.OAuthProviderType;
import com.github.vaadin.oauth.util.OAuthUtil;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import net.anthavio.httl.HttlResponse;
import net.anthavio.httl.HttlResponseExtractor;
import net.anthavio.httl.HttlSender;
import net.anthavio.httl.auth.OAuthTokenResponse;
import net.anthavio.httl.util.HttlUtil;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
@Stateless
@Path("/oauth")
public class OAuthController {

    @Inject
    OAuthUtil oAuthUtil;

    public OAuthController() {

    }

    @Path("/authorize/{provider}")
    @GET
    public String authorize(@PathParam("provider") String provider) {
        OAuthProvider op = oAuthUtil.getByName(provider);
        String url = op.getOAuth().getAuthorizationUrl(op.getScopes(), String.valueOf(System.currentTimeMillis()));
        return "redirect:" + url;
    }

    @Path("/callback/{provider}")
    @GET
    public Response oauthErrorCallback(@PathParam("provider") String provider,
            @QueryParam("error") String error,
            @QueryParam("error_description") String error_description,
            @QueryParam("code") String code,
            @Context HttpServletRequest request) {
        if (error != null) {
            return Response.seeOther(getURI("../main?login_error=" + error)).build();
        }

        OAuthProviderType p = OAuthProvider.getByName(provider);
        OAuthTokenResponse tokenResponse;

        String email;
        switch (p) {
            case FACEBOOK:
                email = facebook(code);
                break;
            case GOOGLE:
                tokenResponse = oAuthUtil.getByName(provider).getOAuth().access(code).get();
                email = google(tokenResponse.getAccess_token());
                break;
            case GITHUB:
                tokenResponse = oAuthUtil.getByName(provider).getOAuth().access(code).get();
                email = github(tokenResponse.getAccess_token());
                break;
            case LINKEDIN:
                tokenResponse = oAuthUtil.getByName(provider).getOAuth().access(code).get();
                email = linkedin(tokenResponse.getAccess_token());
                break;
            default:
                throw new IllegalStateException("Unknown " + p);
        }
        request.getSession().setAttribute("EMAIL", email);
        return Response.seeOther(getURI("../main")).build();
    }

    private String facebook(String code) {
        //https://developers.facebook.com/docs/graph-api/reference/v2.1/user

        Map<String, String> mapOfToken = oAuthUtil.getByName(OAuthProviderType.FACEBOOK.name()).getOAuth().access(code)
                .get(new XFormEncodedExtractor("text/plain"));
        String access_token = mapOfToken.get("access_token");

        HttlSender sender = HttlSender.url("https://graph.facebook.com").build();
        HttlResponseExtractor.ExtractedResponse<Map> response = sender.GET("/me").header("Authorization", "Bearer " + access_token)
                .extract(Map.class);
        Map map = response.getBody();
        return (String) map.get("name");
    }

    private String linkedin(String access_token) {
        //https://developer.linkedin.com/documents/profile-api
        HttlSender sender = HttlSender.url("https://api.linkedin.com").build();
        HttlResponseExtractor.ExtractedResponse<Map> response = sender.GET("/v1/people/~").header("Authorization", "Bearer " + access_token)
                .header("x-li-format", "json").extract(Map.class);
        Map map = response.getBody();
        //System.out.println(map);
        return (String) map.get("firstName") + " " + map.get("lastName");

    }

    private String google(String access_token) {
        HttlSender sender = HttlSender.url("https://www.googleapis.com").build();
        HttlResponseExtractor.ExtractedResponse<Map> response = sender.GET("/plus/v1/people/me")
                .header("Authorization", "Bearer " + access_token).extract(Map.class);
        Map map = response.getBody();
        List<LinkedHashMap> emails = (List<LinkedHashMap>) map.get("emails");
        String email = (String)emails.get(0).get("value");
        return email;
        //return (String) map.get("displayName");
    }

    private String github(String access_token) {
        HttlSender sender = HttlSender.url("https://api.github.com").build();
        HttlResponseExtractor.ExtractedResponse<Map> response = sender.GET("/user").header("Authorization", "token " + access_token)
                .extract(Map.class);
        Map map = response.getBody();
        return (String) map.get("login");
    }

    static class XFormEncodedExtractor implements HttlResponseExtractor<Map<String, String>> {

        private final String mediaType;

        public XFormEncodedExtractor(String mediaType) {
            this.mediaType = mediaType;
        }

        @Override
        public Map<String, String> extract(HttlResponse response) throws IOException {
            int code = response.getHttpStatusCode();
            if (code < 200 || code > 299) {
                throw new IllegalArgumentException("Unexpected status code " + response);
            }
            if (!mediaType.equals(response.getMediaType())) {
                throw new IllegalArgumentException("Unexpected media type " + response);
            }
            Map<String, String> map = new HashMap<>();
            String line = HttlUtil.readAsString(response);
            String[] pairs = line.split("\\&");
            for (String pair : pairs) {
                String[] fields = pair.split("=");
                String name = URLDecoder.decode(fields[0], response.getEncoding());
                String value = URLDecoder.decode(fields[1], response.getEncoding());
                map.put(name, value);
            }
            return map;
        }

    }
    
    private URI getURI(String value) {
        try {
            URI uri = new URI(value);
            return uri;
        } catch (URISyntaxException ex) {
            Logger.getLogger(OAuthController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
