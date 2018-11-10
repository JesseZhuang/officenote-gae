package com.madronabearfacts.helper;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.gmail.Gmail;

import java.util.Properties;

import static com.madronabearfacts.helper.GoogleAuthHelper.*;

/**
 * <p>Eager init singleton for Gmail service.
 * <p>F****** ridiculously simpler than other methods to get auth....
 */
public class GmailSingleton {
    private static final Gmail gmail;

    static {
        Properties p = Constants.GOOGLE;
        gmail = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                new GoogleCredential.Builder().setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT)
                        .setClientSecrets(p.getProperty("client_id_jesse"), p.getProperty("client_secret_jesse")).build()
                        .setAccessToken(p.getProperty("access_token_jesse")).setRefreshToken(p.getProperty("refresh_token_jesse")))
                .setApplicationName(APPLICATION_NAME).build();
    }

    public static Gmail getInstance() {
        return gmail;
    }
}
