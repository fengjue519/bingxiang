package com.smartfridge.service;

import com.google.gson.*;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class DoubaoAiService {

    @Value("${doubao.api.key:}")
    private String apiKey;

    @Value("${doubao.api.url:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String apiUrl;

    @Value("${doubao.model.id:doubao-pro-32k}")
    private String modelId;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    private String getEffectiveApiKey() {
        String envKey = System.getenv("ARK_API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            return envKey;
        }
        return apiKey;
    }

    private String getEffectiveModelId() {
        String envModel = System.getenv("ARK_MODEL_ID");
        if (envModel != null && !envModel.isEmpty()) {
            return envModel;
        }
        return modelId;
    }

    public String chat(String systemPrompt, String userMessage) {
        String effectiveApiKey = getEffectiveApiKey();
        String effectiveModelId = getEffectiveModelId();

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", effectiveModelId);

            JsonArray messages = new JsonArray();

            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", "system");
            systemMsg.addProperty("content", systemPrompt);
            messages.add(systemMsg);

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userMessage);
            messages.add(userMsg);

            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 2000);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + effectiveApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json; charset=utf-8")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    return "AI服务请求失败 (HTTP " + response.code() + "): " + responseBody;
                }

                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                if (jsonResponse.has("error")) {
                    JsonObject error = jsonResponse.getAsJsonObject("error");
                    return "API错误: " + error.toString();
                }

                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    if (firstChoice.has("message")) {
                        JsonObject message = firstChoice.getAsJsonObject("message");
                        return message.get("content").getAsString();
                    }
                }

                return "AI返回格式异常: " + responseBody;
            }
        } catch (JsonSyntaxException e) {
            return "AI响应解析失败: " + e.getMessage();
        } catch (IOException e) {
            return "AI服务连接失败: " + e.getMessage();
        } catch (Exception e) {
            return "AI服务异常: " + e.getMessage();
        }
    }

    public String chatWithImage(String systemPrompt, String userMessage, String base64Image) {
        String effectiveApiKey = getEffectiveApiKey();
        String effectiveModelId = getEffectiveModelId();

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", effectiveModelId);

            JsonArray messages = new JsonArray();

            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", "system");
            systemMsg.addProperty("content", systemPrompt);
            messages.add(systemMsg);

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");

            JsonArray contentArray = new JsonArray();

            JsonObject textContent = new JsonObject();
            textContent.addProperty("type", "text");
            textContent.addProperty("text", userMessage);
            contentArray.add(textContent);

            if (base64Image != null && !base64Image.isEmpty()) {
                JsonObject imageContent = new JsonObject();
                imageContent.addProperty("type", "image_url");

                JsonObject imageUrlObj = new JsonObject();
                imageUrlObj.addProperty("url", "data:image/jpeg;base64," + base64Image);
                imageContent.add("image_url", imageUrlObj);

                contentArray.add(imageContent);
            }

            userMsg.add("content", contentArray);
            messages.add(userMsg);

            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 2000);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + effectiveApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json; charset=utf-8")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    return "AI服务请求失败 (HTTP " + response.code() + "): " + responseBody;
                }

                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                if (jsonResponse.has("error")) {
                    JsonObject error = jsonResponse.getAsJsonObject("error");
                    return "API错误: " + error.toString();
                }

                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    if (firstChoice.has("message")) {
                        JsonObject message = firstChoice.getAsJsonObject("message");
                        return message.get("content").getAsString();
                    }
                }

                return "AI返回格式异常: " + responseBody;
            }
        } catch (JsonSyntaxException e) {
            return "AI响应解析失败: " + e.getMessage();
        } catch (IOException e) {
            return "AI服务连接失败: " + e.getMessage();
        } catch (Exception e) {
            return "AI服务异常: " + e.getMessage();
        }
    }

    public boolean isConfigured() {
        String effectiveKey = getEffectiveApiKey();
        return effectiveKey != null && !effectiveKey.isEmpty() && !effectiveKey.equals("your-api-key-here");
    }

    public String testConnection() {
        return chat("你是一个友好的助手。请用一句话介绍自己。", "你好");
    }
}
