import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

public class StringAnalyzer implements Analyzer<String>{

    private static final String rawInputPath = "rawHtml.txt";
    private static final String htmlCleanedOutputPath = "cleanedHtml.txt";
    private static final String geminiBasePromptInputPath = "baseGeminiPrompt.txt";
    private static final String geminiApiOutputPath = "geminiOutput.txt";
    private static final String geminiPromptOutputPath = "geminiPromptOutput.txt";

    /**
     * Выполняет анализ заданного исходного набора данных:
     * 1. Считывает «сырое» HTML из файла {@value #rawInputPath}.
     * 2. Удаляет теги {@code <noscript>}, {@code <script>}, {@code <style>}, {@code <iframe>}
     * 3. Пишет очищенный текст в {@value #htmlCleanedOutputPath}.
     * 4. Считывает конфигурацию из properties «object», «additions», «targets».
     * 5. Строит prompt по шаблону из {@value #geminiBasePromptInputPath}.
     * 6. Записывает финальный prompt в {@value #geminiPromptOutputPath}.
     * 7. Вызывает Gemini API через {@link ApiCaller#makeApiCall(String, String)}.
     * 8. Пишет ответ LLM в {@value #geminiApiOutputPath}.
     *
     * @param data исходный набор данных для анализа. Обязательно ненулевой,
     *             и {@link Data#getContent()} тоже не должен быть null.
     * @return Пока всегда возвращает null (запись результата производится в файл).
     *         В будущем здесь должен возвращаться {@code Data<String>}
     *         с результатом анализа (LLM-ответом).
     * @throws IntegratorException если {@code data} или его содержимое null.
     * @throws RuntimeException    если падает чтение/запись файлов или сборка prompt.
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

        String rawHtml = FileHandler.readFromFile(rawInputPath);

        // remove HTML tags
        Document doc = Jsoup.parse(rawHtml);
        doc.select("noscript, script, style, iframe, link[rel=stylesheet], meta, head title")
                .remove();
        String processedHtml = doc.text();
        FileHandler.writeToFile(htmlCleanedOutputPath, processedHtml);

        // read config
        Map<String, String> configValues =
                FileHandler.readSpecificProperties("object", "additions", "targets");
        String objectValue = configValues.get("object");
        String targetsValue = configValues.get("targets");

        String promptTemplate = FileHandler.readFromFile(geminiBasePromptInputPath);
        // assemble final prompt
        String finalGeminiPrompt = promptTemplate
                .replace("{{ИСХОДНЫЙ_ТЕКСТ}}", processedHtml)
                .replace("{{OBJECT_VALUES}}", objectValue)
                .replace("{{TARGETS_VALUES}}", targetsValue);

        FileHandler.writeToFile(geminiPromptOutputPath,finalGeminiPrompt);
        String geminiOutput = ApiCaller.makeApiCall(processedHtml, finalGeminiPrompt);
        FileHandler.writeToFile(geminiApiOutputPath, geminiOutput);

        //возвращаю null, потому что реализации Data у меня нет. Это нестрашно, потому что запись в файл результата
        //все равно будет
        return null;
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
