package com.example.hanul.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DialogflowService {
    private final SessionsClient sessionsClient;

    public DialogflowService() throws IOException {
        // JSON 키 파일의 경로와 이름에 맞게 수정
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream jsonKeyStream = classLoader.getResourceAsStream("hanulwellness-odgf-9b92e16f6ac1.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(jsonKeyStream);

        SessionsSettings sessionsSettings =
                SessionsSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                        .build();

        this.sessionsClient = SessionsClient.create(sessionsSettings);
    }

    public DetectIntentResponse sendToDialogflow(String userMessage) throws IOException {
        String projectId = "hanulwellness-odgf";
        String sessionId = "eb9aab8b936ace3a66f80d5842b3f659ca7eb699";

        SessionName session = SessionName.of(projectId, sessionId);

        TextInput.Builder textInput = TextInput.newBuilder()
                .setText(userMessage)
                .setLanguageCode("ko-KR"); // 한국어 언어 코드 설정

        QueryInput queryInput = QueryInput.newBuilder()
                .setText(textInput)
                .build();

        DetectIntentResponse response = sessionsClient.detectIntent(session.toString(), queryInput);

        return response;
    }

    public DetectIntentResponse sendRecommendToDialogflow(String userMessage, String emotion) throws IOException {
        String projectId = "hanulwellness-odgf";
        String sessionId = "eb9aab8b936ace3a66f80d5842b3f659ca7eb699";

        SessionName session = SessionName.of(projectId, sessionId);

        TextInput.Builder textInput = TextInput.newBuilder()
                .setText(userMessage)
                .setLanguageCode("ko-KR"); // 한국어 언어 코드 설정

        // EventInput 설정
        EventInput eventInput = EventInput.newBuilder()
                .setName(emotion) // 원하는 이벤트 이름 설정
                .setLanguageCode("ko-KR")
                .build();

        QueryInput queryInput = QueryInput.newBuilder()
                .setText(textInput)
                .setEvent(eventInput)
                .build();

        DetectIntentResponse response = sessionsClient.detectIntent(session.toString(), queryInput);

        return response;
    }
}
