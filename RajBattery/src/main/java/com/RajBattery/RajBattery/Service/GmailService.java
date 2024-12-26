//package com.RajBattery.RajBattery.Service;
//
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.gmail.Gmail;
//import com.google.api.services.gmail.GmailScopes;
//import com.google.api.services.gmail.model.Message;
//
//import java.io.*;
//import java.security.GeneralSecurityException;
//import java.util.Collections;
//import java.util.List;
//
//public class GmailService {
//
//    private static final String APPLICATION_NAME = "YourAppName";
//    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
//    private static final String CREDENTIALS_FILE_PATH = "/path/to/credentials.json";
//    private static final String TOKENS_DIRECTORY_PATH = "tokens";
//
//    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//                .setAccessType("offline")
//                .build();
//        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
//    }
//
//    public static List<Message> fetchEmails() throws GeneralSecurityException, IOException {
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//
//        // Fetch messages from the Gmail inbox
//        List<Message> messages = service.users().messages().list("me").setQ("is:unread").execute().getMessages();
//
//        return messages;
//    }
//}
