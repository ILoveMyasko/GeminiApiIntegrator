import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
/**
 * Утилитный класс для выполнения запросов к LLM Gemini через API-клиент {@link Client}.
 * <p>
 * Предоставляет статический метод для генерации контента на основе объединения
 * переданного шаблона prompt и подготовленного HTML.
 */
public class ApiCaller {
    /**
     * @param processedHtml предварительно обработанный HTML-контент; не может быть {@code null}
     * @param prompt        полный промпт для LLM; не может быть {@code null}
     * @return текстовое содержимое ответа LLM, или {@code null} в случае ошибки API-клиента или иной проблемы
     * @throws IllegalArgumentException если {@code processedHtml == null} или {@code prompt == null}
     */
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

}
