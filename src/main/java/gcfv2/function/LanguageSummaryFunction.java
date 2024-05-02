package gcfv2.function;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.preview.ChatSession;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gcfv2.repository.LanguageCounterRepositoryImpl;
import gcfv2.repository.LanguageCounterRepository;

import java.io.IOException;
import java.util.Optional;

import static gcfv2.util.Constant.*;

public class LanguageSummaryFunction implements HttpFunction {

    private final LanguageCounterRepository languageCounterRepository;
    private final Gson gson;

    public LanguageSummaryFunction() {
        this.languageCounterRepository = new LanguageCounterRepositoryImpl();
        this.gson = new Gson();
    }

    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        Optional<JsonObject> requestBody = Optional.ofNullable(gson.fromJson(httpRequest.getReader(), JsonObject.class));

        if (requestBody.isEmpty()) {
            httpResponse.setContentType(CONTENT_TYPE);
            httpResponse.setStatusCode(BAD_REQUEST_STATUS_CODE);
            httpResponse.getWriter().write(REQUEST_BODY_IS_EMPTY);
            return;
        }

        Optional<JsonElement> languageOpt = Optional.ofNullable(requestBody.get().get(LANGUAGE));

        if (languageOpt.isEmpty()) {
            httpResponse.setContentType(CONTENT_TYPE);
            httpResponse.setStatusCode(BAD_REQUEST_STATUS_CODE);
            httpResponse.getWriter().write(INVALID_JSON);
            return;
        }

        String language = languageOpt.get().getAsString();
        String message = String.format(
                MESSAGE_FOR_GEMINI_PATTERN,
                language, LANGUAGE_NOT_EXIST);

        try {
            String geminiResponse = sendMessageToGemini(message);

            JsonObject responseBody = new JsonObject();
            responseBody.addProperty(RESPONSE, geminiResponse);

            if (!geminiResponse.contains(LANGUAGE_NOT_EXIST)) {
                languageCounterRepository.incrementCounter(language.toLowerCase());
            }

            httpResponse.setContentType(CONTENT_TYPE);
            httpResponse.getWriter().write(gson.toJson(responseBody));
            httpResponse.setStatusCode(OK_STATUS_CODE);
        } catch (IOException e) {
            httpResponse.setContentType(CONTENT_TYPE);
            httpResponse.setStatusCode(INTERNAL_SERVER_ERROR_STATUS_CODE);
            httpResponse.getWriter().write(e.getMessage());
        }
    }

    /**
     * Sends a message to the Gemini chatbot hosted on Vertex AI and retrieves the response.
     *
     * @param message The message to send to the Gemini chatbot.
     * @return The response from the Gemini chatbot.
     * @throws IOException If an I/O error occurs while communicating with the Gemini chatbot.
     */
    private String sendMessageToGemini(String message) throws IOException {
        VertexAI vertexAi = new VertexAI(PROJECT_ID, LOCATION);

        GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAi);

        ChatSession chatSession = new ChatSession(model);

        GenerateContentResponse response = chatSession.sendMessage(message);
        return ResponseHandler.getText(response);
    }
}