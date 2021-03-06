/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.tasks.TasksScopes;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author thorsten
 */
public class GoogleAuthorizationHelper {
    
    /** OAuth client ID JSON */
    public final static String CLIENT_ID = "{\"web\":{\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"client_secret\":\"YI5-RHyCR5g3ka8EONQosdqg\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"client_email\":\"742875388091-gi4eube4sr25jd94doluln4jrd62i37t@developer.gserviceaccount.com\",\"redirect_uris\":[\"https://localhost:8181/fridget/gauth\",\"https://www.doogetha.com/fridget/gauth\"],\"client_x509_cert_url\":\"https://www.googleapis.com/robot/v1/metadata/x509/742875388091-gi4eube4sr25jd94doluln4jrd62i37t@developer.gserviceaccount.com\",\"client_id\":\"742875388091-gi4eube4sr25jd94doluln4jrd62i37t.apps.googleusercontent.com\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"javascript_origins\":[\"https://localhost:8181\",\"https://www.doogetha.com\"]}}";
    
    /* Scopes */
    public final static Collection<String> SCOPES = Arrays.asList(
            CalendarScopes.CALENDAR_READONLY,
            TasksScopes.TASKS_READONLY
    );
    
    public final static String APPLICATION_NAME = "Fridget";
    
    /** Global instance of the HTTP transport. */
    public static HttpTransport HTTP_TRANSPORT;
    
    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
    
    private AuthorizationCodeFlow flow = null;
    
    private String userId = null;
    
    public GoogleAuthorizationHelper(String userId) {
        try {
            GoogleClientSecrets secrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new StringReader(CLIENT_ID));
            this.userId = userId;
            this.flow =
                    new GoogleAuthorizationCodeFlow.Builder(
                            HTTP_TRANSPORT,
                            JacksonFactory.getDefaultInstance(),
                            secrets,
                            SCOPES)
                           .setDataStoreFactory(new FileDataStoreFactory(new File("google-calendar-credentials/"+userId)))
                           .setAccessType("offline")
                           .build();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public boolean hasCredentials() {
        Credential credential = getCredentials();
        return (credential != null && credential.getAccessToken() != null); 
    }
    
    public String getRefreshToken() {
        if (!hasCredentials()) return null;
        return getCredentials().getRefreshToken();
    }
    
    public Credential getCredentials() {
        try {
            Credential credential = flow.loadCredential(userId);
            return credential;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getAuthorizationUrl(String url) {
        return flow.newAuthorizationUrl().setRedirectUri(url).build();
    }
    
    public void handleAuthorizationCodeResult(String resultUrl, String redirectUrl) throws IOException {
        AuthorizationCodeResponseUrl responseUrl = new AuthorizationCodeResponseUrl(resultUrl);
        String code = responseUrl.getCode();
        if (responseUrl.getError() != null || code == null) {
          throw new IllegalArgumentException("Error response received.");
        } else {
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUrl).execute();
            Credential credential = flow.createAndStoreCredential(response, userId);
            System.out.println(credential);
        }
    }
}
