package br.com.alura.screenmatch.service;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;

public class ConsultaGPT {
    public static String obterTraducao(String texto) {
        OpenAiService service = new OpenAiService(System.getenv("OPEN_IA_API_KEY"));

        CompletionRequest requisicao = CompletionRequest.builder()
                .model("gpt-3.5-turbo-instruct")
                .prompt("Traduza para o português o texto: " + texto)
                .maxTokens(500)
                .temperature(0.7)
                .build();

        var resposta = service.createCompletion(requisicao);
        return resposta.getChoices().get(0).getText();
    }
}
