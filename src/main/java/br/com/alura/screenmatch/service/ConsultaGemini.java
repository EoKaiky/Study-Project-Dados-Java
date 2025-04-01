package br.com.alura.screenmatch.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

public class ConsultaGemini {
    public static String obterTraducao(String texto) {

        String apiKey = System.getenv("OPENIA_APIKEY");

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("A chave da API não foi definida como variável de ambiente.");
        }

        ChatLanguageModel gemini = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-1.5-flash")
                .build();

        String prompt = "Traduza este texto para o português: " + texto;
        String response = gemini.generate(prompt);
        return response;
    }

}
