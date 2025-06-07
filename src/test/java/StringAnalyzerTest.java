
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StringAnalyzerTest {


    private StringAnalyzer analyzer;
    private MockedStatic<FileHandler> fh;
    private MockedStatic<ApiCaller> api;

    @BeforeEach
    void setUp() {
        analyzer = new StringAnalyzer();
        fh  = mockStatic(FileHandler.class);
        api = mockStatic(ApiCaller.class);
    }

    @AfterEach
    void tearDown() {
        fh.close();
        api.close();
    }

    @Test
    void analyze_nullData_throws() {
        assertThrows(IntegratorException.class, () -> analyzer.analyze((Data<String>) null));
    }

    @Test
    void analyze_nullContent_throws() throws IntegratorException {
        Data<String> d = mock(Data.class);
        when(d.getContent()).thenReturn(null);
        assertThrows(IntegratorException.class, () -> analyzer.analyze(d));
    }

    @Test
    void analyze_happyPath_writesFilesAndCallsApi() throws Exception {

        String rawHtml = "<p>Hello</p><script>bad()</script>";
        Data<String> input = mock(Data.class);
        when(input.getContent()).thenReturn("irrelevant");  // метод использует только rawHtml

        fh.when(() -> FileHandler.readFromFile("rawHtml.txt"))
                .thenReturn(rawHtml);

        fh.when(() -> FileHandler.readSpecificProperties("object", "additions", "targets"))
                .thenReturn(Map.of(
                        "object",  "OBJ",
                        "targets", "TGT"
                ));
        String template = "BASE: {{ИСХОДНЫЙ_ТЕКСТ}} / {{OBJECT_VALUES}} / {{TARGETS_VALUES}}";
        fh.when(() -> FileHandler.readFromFile("baseGeminiPrompt.txt"))
                .thenReturn(template);

        String fakeApiResult = "GPT says OK";
        api.when(() -> ApiCaller.makeApiCall(anyString(), anyString()))
                .thenReturn(fakeApiResult);

        Data<String> result = analyzer.analyze(input);
        assertNull(result);

        fh.verify(() -> FileHandler.writeToFile(
                "cleanedHtml.txt", "Hello"
        ));

        String expectedPrompt = template
                .replace("{{ИСХОДНЫЙ_ТЕКСТ}}", "Hello")
                .replace("{{OBJECT_VALUES}}",  "OBJ")
                .replace("{{TARGETS_VALUES}}", "TGT");
        fh.verify(() -> FileHandler.writeToFile(
                "geminiPromptOutput.txt", expectedPrompt
        ));

        fh.verify(() -> FileHandler.writeToFile(
                "geminiOutput.txt", fakeApiResult
        ));

        api.verify(() -> ApiCaller.makeApiCall("Hello", expectedPrompt));
    }

    @Test
    void analyze_readRawHtmlThrows_throwsRuntimeException() throws IntegratorException {
        Data<String> input = mock(Data.class);
        when(input.getContent()).thenReturn("x");

        // FileHandler.readFromFile выбросит RuntimeException
        fh.when(() -> FileHandler.readFromFile("rawHtml.txt"))
                .thenThrow(new RuntimeException("disk error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> analyzer.analyze(input)
        );
        assertTrue(ex.getMessage().contains("disk error"));
    }

    // 1) Ситуация, когда чтение свойств падает
    @Test
    void analyze_readPropertiesThrows_throwsRuntimeException() throws IntegratorException {
        Data<String> input = mock(Data.class);
        when(input.getContent()).thenReturn("x");

        // нормальное чтение rawHtml
        fh.when(() -> FileHandler.readFromFile("rawHtml.txt"))
                .thenReturn("<p>Foo</p>");
        // сбой при чтении properties
        fh.when(() -> FileHandler.readSpecificProperties("object", "additions", "targets"))
                .thenThrow(new RuntimeException("props fail"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> analyzer.analyze(input)
        );
        assertTrue(ex.getMessage().contains("props fail"));
    }

    // 2) Сценарий, когда шаблон для prompt не найден / падает
    @Test
    void analyze_readBasePromptThrows_throwsRuntimeException() throws IntegratorException {
        Data<String> input = mock(Data.class);
        when(input.getContent()).thenReturn("x");

        fh.when(() -> FileHandler.readFromFile("rawHtml.txt"))
                .thenReturn("<p>Bar</p>");
        fh.when(() -> FileHandler.readSpecificProperties("object", "additions", "targets"))
                .thenReturn(Map.of("object","O","targets","T"));
        // сбой на чтении шаблона
        fh.when(() -> FileHandler.readFromFile("baseGeminiPrompt.txt"))
                .thenThrow(new RuntimeException("template missing"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> analyzer.analyze(input)
        );
        assertTrue(ex.getMessage().contains("template missing"));
    }

    // 3) Если ApiCaller возвращает null — всё равно должно записаться в файл
    @Test
    void analyze_apiReturnsNull_writesNullOutput() throws Exception {
        String html = "<div>Alpha</div>";
        Data<String> input = mock(Data.class);
        when(input.getContent()).thenReturn("irrelevant");

        fh.when(() -> FileHandler.readFromFile("rawHtml.txt"))
                .thenReturn(html);
        fh.when(() -> FileHandler.readSpecificProperties("object", "additions", "targets"))
                .thenReturn(Map.of("object","OBJ","targets","TGT"));
        fh.when(() -> FileHandler.readFromFile("baseGeminiPrompt.txt"))
                .thenReturn("P: {{ИСХОДНЫЙ_ТЕКСТ}}");
        // API вернул null
        api.when(() -> ApiCaller.makeApiCall(anyString(), anyString()))
                .thenReturn(null);

        Data<String> result = analyzer.analyze(input);
        assertNull(result);

        // проверяем, что в файл geminiOutput.txt записали именно null
        fh.verify(() -> FileHandler.writeToFile("geminiOutput.txt", null));
    }

    // 4) Сбой при записи одного из файлов
    @Test
    void analyze_writeToFileThrows_throwsRuntimeException() throws IntegratorException {
        Data<String> input = mock(Data.class);
        when(input.getContent()).thenReturn("x");

        fh.when(() -> FileHandler.readFromFile("rawHtml.txt"))
                .thenReturn("<p>Data</p>");
        fh.when(() -> FileHandler.readSpecificProperties("object", "additions", "targets"))
                .thenReturn(Map.of("object","O","targets","T"));
        fh.when(() -> FileHandler.readFromFile("baseGeminiPrompt.txt"))
                .thenReturn("T: {{ИСХОДНЫЙ_ТЕКСТ}}");
        api.when(() -> ApiCaller.makeApiCall(anyString(), anyString()))
                .thenReturn("out");

        // первый writeToFile пройдёт нормально
        // на втором (чистый HTML) упадёт
        fh.when(() -> FileHandler.writeToFile("cleanedHtml.txt", "Data"))
                .thenThrow(new RuntimeException("disk fail"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> analyzer.analyze(input)
        );
        assertTrue(ex.getMessage().contains("disk fail"));
    }

    // 5) Проверяем, что Jsoup корректно убирает разные теги
    @Test
    void analyze_htmlWithVariousTags_cleansThemAll() throws Exception {
        String rawHtml = "<html><head><title>t</title><style>c{}</style></head>"
                + "<body><script>bad</script><noscript>bad2</noscript>"
                + "<iframe/>Good<link rel=\"stylesheet\"/></body></html>";

        Data<String> input = mock(Data.class);
        when(input.getContent()).thenReturn("irrelevant");

        fh.when(() -> FileHandler.readFromFile("rawHtml.txt"))
                .thenReturn(rawHtml);
        fh.when(() -> FileHandler.readSpecificProperties("object", "additions", "targets"))
                .thenReturn(Map.of("object","O","targets","T"));
        fh.when(() -> FileHandler.readFromFile("baseGeminiPrompt.txt"))
                .thenReturn("P: {{ИСХОДНЫЙ_ТЕКСТ}}");
        api.when(() -> ApiCaller.makeApiCall(anyString(), anyString()))
                .thenReturn("ok");

        analyzer.analyze(input);

        // остаётся только слово "Good"
        fh.verify(() -> FileHandler.writeToFile("cleanedHtml.txt", "Good"));
    }



}