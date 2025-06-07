

import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

import com.google.genai.types.ThinkingConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Main {


    public static void main(String[] args) {
        // The client gets the API key from the environment variable `GOOGLE_API_KEY`.
        final String rawInputPath = "rawHtml.txt";
        final String htmlCleanedOutputPath = "cleanedHtml.txt";
        final String geminiBasePromptInputPath = "baseGeminiPrompt.txt";
        final String geminiApiOutputPath = "geminiOutput.txt";
        final String geminiPromptOutputPath = "geminiPromptOutput.txt";

        String rawHtml = readFromFile(rawInputPath);
        Document doc = Jsoup.parse(rawHtml);
        doc.select("noscript, script, style, iframe, link[rel=stylesheet], meta, head title")
                .remove();

        String processedHtml = doc.text();
        writeToFile(htmlCleanedOutputPath, processedHtml);

        Map<String, String> configValues =
                ConfigReader.readSpecificProperties("object", "additions", "targets");
        String objectValue = configValues.get("object");
        String targetsValue = configValues.get("targets");

        String promptTemplate = readFromFile(geminiBasePromptInputPath);
        String finalGeminiPrompt = promptTemplate
                .replace("{{ИСХОДНЫЙ_ТЕКСТ}}", processedHtml)
                .replace("{{OBJECT_VALUES}}", objectValue)
                .replace("{{TARGETS_VALUES}}", targetsValue);

        writeToFile(geminiPromptOutputPath,finalGeminiPrompt);
        String geminiOutput = makeApiCall(processedHtml, finalGeminiPrompt);
        writeToFile(geminiApiOutputPath, geminiOutput);

    }

    public static void writeToFile(String outputFilePath, String content) {
        try {
            Files.writeString(Paths.get(outputFilePath), content);
            System.out.println("Успешно записал в файл: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Ошибка: Не удалось записать  в файл '" + outputFilePath + "': " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }

    public static String readFromFile(String inputFilePath) {
        String output = "";
        try {
            output = Files.readString(Paths.get(inputFilePath));
        } catch (IOException e) {
            System.err.println("Критическая ошибка: Не удалось прочитать файл "
                    + inputFilePath + "': " + e.getMessage());
            throw new RuntimeException("Ошибка чтения файла: " + inputFilePath, e);
        }
        return output;
    }

    public static String makeApiCall(String processedHtml, String prompt) {
        final String modelName = "gemini-2.5-flash-preview-05-20";
        Client client = new Client();
        GenerateContentResponse response = null;
        try {
            String fullPrompt = prompt + processedHtml;
            response = client.models.generateContent(modelName, fullPrompt,
                    GenerateContentConfig.builder()
                            .temperature(0.05f)
                            .topP(0.95f)
                            .build());
        } catch (ClientException e) {
            System.err.println("Ошибка клиента API: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка при вызове API или обработке ответа: " + e.getMessage());
        }
        if (response == null) {
            throw new RuntimeException("Error communicating with Gemini API client");
        }
        return response.text();
    }



}



