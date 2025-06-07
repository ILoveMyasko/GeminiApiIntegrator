import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class StringAnalyzer implements Analyzer<String>{

    private static final String rawInputPath = "rawHtml.txt";
    private static final String htmlCleanedOutputPath = "cleanedHtml.txt";
    private static final String geminiBasePromptInputPath = "baseGeminiPrompt.txt";
    private static final String geminiApiOutputPath = "geminiOutput.txt";
    private static final String geminiPromptOutputPath = "geminiPromptOutput.txt";
    /**
     * Метод выполняет анализ заданного исходного набора данных.
     *
     * @param data исходный набор данных для анализа.
     * @return результат анализа исходного набора данных в формате исходного набора данных.
     * @throws IntegratorException выбрасывается в случае невозможности выполнить анализ данных.
     */
    @Override
    public Data<String> analyze(Data<String> data) throws IntegratorException {
        if (data == null) {
            throw new IntegratorException("Data is null");
        }
        String htmlText = data.getContent();
        if (htmlText == null) {
            throw new IntegratorException("Data is null");
        }

        String rawHtml = readFromFile(rawInputPath);

        // remove HTML tags
        Document doc = Jsoup.parse(rawHtml);
        doc.select("noscript, script, style, iframe, link[rel=stylesheet], meta, head title")
                .remove();
        String processedHtml = doc.text();
        writeToFile(htmlCleanedOutputPath, processedHtml);

        // read config
        Map<String, String> configValues =
                ConfigReader.readSpecificProperties("object", "additions", "targets");
        String objectValue = configValues.get("object");
        String targetsValue = configValues.get("targets");

        String promptTemplate = readFromFile(geminiBasePromptInputPath);
        // assemble final prompt
        String finalGeminiPrompt = promptTemplate
                .replace("{{ИСХОДНЫЙ_ТЕКСТ}}", processedHtml)
                .replace("{{OBJECT_VALUES}}", objectValue)
                .replace("{{TARGETS_VALUES}}", targetsValue);

        writeToFile(geminiPromptOutputPath,finalGeminiPrompt);
        String geminiOutput = makeApiCall(processedHtml, finalGeminiPrompt);
        writeToFile(geminiApiOutputPath, geminiOutput);

        return null;
    }

    public static void writeToFile(String outputFilePath, String content) {
        if (outputFilePath == null) {
            throw new NullPointerException("Путь к файлу не может быть null");
        }
        if (content == null) {
            throw new NullPointerException("Содержимое для записи не может быть null");
        }
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
        try {
            return Files.readString(Paths.get(inputFilePath));
        } catch (IOException e) {
            System.err.println("Критическая ошибка: Не удалось прочитать файл "
                    + inputFilePath + "': " + e.getMessage());
            throw new RuntimeException("Ошибка чтения файла: " + inputFilePath, e);
        }
    }

    public static String makeApiCall(String processedHtml, String prompt) {

        if (processedHtml == null || prompt == null) {
            System.err.println("Ошибка: processedHtml или prompt не могут быть null.");
            throw new IllegalArgumentException("Ошибка: processedHtml или prompt не могут быть null.");
        }

        final String modelName = "gemini-2.5-flash-preview-05-20";
        GenerateContentResponse response;
        String responseText = null;
        try (Client client = new Client()){
            String fullPrompt = prompt + processedHtml;
            response = client.models.generateContent(modelName, fullPrompt,
                    GenerateContentConfig.builder()
                            .temperature(0.05f)
                            .topP(0.95f)
                            .build());
            responseText = response.text();
        } catch (ClientException e) {
            System.err.println("Ошибка клиента API: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка при вызове API или обработке ответа: " + e.getMessage());
        }
        return responseText;
    }


    /**
     * Метод выполняет анализ источника данных без преобразования исходного типа данных.
     *
     * @param source источник данных для анализа.
     * @return возвращается результат анализа в исходном формате источника данных.
     * @throws IntegratorException выбрасывается в случае невозможности выполнить анализ данных.
     */
    @Override
    public Data<String> analyze(Source<String> source) throws IntegratorException {
        return null;
    }

    /**
     * Метод выполняет анализ источника данных с преобразованием исходного типа данных.
     *
     * @param source    источник данных для анализа.
     * @param converter ссылка на объект, который используется для преобразования данных из исходного формата
     *                  T в формат U.
     * @return результат анализа данных источника в формате U.
     * @throws IntegratorException выбрасывается в случае невозможности выполнить анализ данных.
     */
    @Override
    public <U> Data<U> analyze(Source<String> source, Converter<String, U> converter) throws IntegratorException {
        return null;
    }


    /**
     * Метод выполняет анализ заданного исходного набора данных с преобразованием исходного типа данных.
     *
     * @param data      исходный набор данных для анализа.
     * @param converter ссылка на объект, который используется для преобразования данных из исходного формата
     *                  T в формат U.
     * @return результат анализа исходного набора данных в формате U.
     * @throws IntegratorException выбрасывается в случае невозможности выполнить анализ данных.
     */
    @Override
    public <U> Data<U> analyze(Data<String> data, Converter<String, U> converter) throws IntegratorException {
        return null;
    }
}
