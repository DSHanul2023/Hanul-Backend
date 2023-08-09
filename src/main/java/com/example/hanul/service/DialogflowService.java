package com.example.hanul.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Service
public class DialogflowService {
    private final SessionsClient sessionsClient;

    public DialogflowService() throws IOException {
        // JSON 키 파일의 경로와 이름에 맞게 수정
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("hanulwellness-odgf-9b92e16f6ac1.json"));

        SessionsSettings sessionsSettings =
                SessionsSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                        .build();

        this.sessionsClient = SessionsClient.create(sessionsSettings);
    }

    public String sendToDialogflow(String userMessage) throws IOException {
        String projectId = "hanulwellness-odgf";
        String sessionId = "eb9aab8b936ace3a66f80d5842b3f659ca7eb699";

        SessionName session = SessionName.of(projectId, sessionId);

        TextInput.Builder textInput = TextInput.newBuilder().setText(userMessage);
        QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

        DetectIntentResponse response = sessionsClient.detectIntent(session.toString(), queryInput);

        return response.getQueryResult().getFulfillmentText();
    }
}
