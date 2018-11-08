package com.madronabearfacts.helper;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class GoogleAuthHelper {
    /**
     * Application name.
     */
    protected static final String APPLICATION_NAME = "Madrona Office Note";
    /**
     * Global instance of the JSON factory.
     */
    protected static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /**
     * Gmail credential json key file path.
     */
    private static final String GMAIL_CRED_JSON_PATH = "/madrona.gmail.credentials.json";

    /**
     * Directory to store user credentials for this application.
     */
    private static final String DATA_STORE_PATH = "gmail_stored_cred";

    /**
     * Global instance of the HTTP transport.
     */
    protected static HttpTransport HTTP_TRANSPORT;

    private static final List<String> GMAIL_SCOPES = Arrays.asList(GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_SEND);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static Gmail getGmailServiceFromClassPath() throws IOException {
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredFromClassPath(GMAIL_SCOPES))
                .setApplicationName(APPLICATION_NAME).build();
    }

    @SuppressWarnings("unused")
    public static Gmail getGmailServiceFromFilePath() throws IOException {
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredFromFilePath(GMAIL_SCOPES))
                .setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Fucking ridiculously simpler than other methods to get auth....
     *
     * @return Gmail service with oauth.
     */
    public static Gmail getGmailService() {
        Properties p = Constants.GOOGLE;
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                new GoogleCredential.Builder().setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT)
                        .setClientSecrets(p.getProperty("client_id"), p.getProperty("client_secret")).build()
                        .setAccessToken(p.getProperty("access_token")).setRefreshToken(p.getProperty("refresh_token")))
                .setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Get credential with json key for google oAuth2 which needs login to confirm.
     * Credential cache is then saved in DATA_STORE_PATH.
     */
    private static Credential getCredFromClassPath(Collection<String> scopes) throws IOException {
        InputStream in = GoogleAuthHelper.class.getResourceAsStream(GMAIL_CRED_JSON_PATH);
        return getCred(scopes, in, DATA_STORE_PATH);
    }

    private static Credential getCredFromFilePath(Collection<String> scopes) throws IOException {
        InputStream in = new FileInputStream(Constants.resouceFilePath + GMAIL_CRED_JSON_PATH);
        return getCred(scopes, in, Constants.resouceFilePath + DATA_STORE_PATH);
    }

    private static Credential getCred(Collection<String> scopes, InputStream in, String dataStorePath) throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        File credFile = new File(dataStorePath);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes)
//                .setDataStoreFactory(new FileDataStoreFactory(credFile))
                .setDataStoreFactory(AppEngineDataStoreFactory.getDefaultInstance())
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + credFile.getAbsolutePath());
        return credential;
    }

    @Deprecated // regular gmail account does not support service account
    public static Gmail getGmailServiceAccountFromFilePath() throws IOException {
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredServiceAccountFromFilePath(GMAIL_SCOPES))
                .setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Get credential with json key generated from the service account.
     * Scope example: gmail, google calendar, and google drive.
     *
     * @return GoogleCredential for a scope.
     */
    public static GoogleCredential getCredServiceAccountFromClassPath(String scope) throws IOException {
        return GoogleCredential
                .fromStream(GoogleAuthHelper.class.getResourceAsStream("/officenote-gae-madrona.json"))
                .createScoped(Collections.singleton(scope));
    }

    public static GoogleCredential getCredServiceAccountFromFilePath(Collection<String> scopes) throws IOException {
        GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream("src/main/resources/officenote-gae-madrona.json"))
                .createScoped(scopes);
        return credential;
    }

}