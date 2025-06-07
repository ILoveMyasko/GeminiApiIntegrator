import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.errors.ClientException;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockConstruction;

class ApiCallerTest {


    @Test
    void whenProcessedHtmlIsNull_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> ApiCaller.makeApiCall(null, "prompt"));
    }

    @Test
    void whenPromptIsNull_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> ApiCaller.makeApiCall("<html/>", null));
    }

    @Test
    void whenApiReturnsResponse_thenReturnsResponseText() throws Exception {
        String fakeHtml      = "<p>test</p>";
        String fakePrompt    = "Tell me about: ";
        String expectedText  = "LLM answer";

        try (MockedConstruction<Client> ignored = mockConstruction(Client.class, (mockClient, context) -> {
            Models mockModels = mock(Models.class);

            Field modelsField = Client.class.getField("models");
            modelsField.setAccessible(true);
            modelsField.set(mockClient, mockModels);

            GenerateContentResponse fakeResp = mock(GenerateContentResponse.class);
            when(fakeResp.text()).thenReturn(expectedText);

            when(mockModels.generateContent(
                    eq("gemini-2.5-flash-preview-05-20"),
                    eq(fakePrompt + fakeHtml),
                    any(GenerateContentConfig.class)))
                    .thenReturn(fakeResp);
        })) {
            String actual = ApiCaller.makeApiCall(fakeHtml, fakePrompt);
            assertEquals(expectedText, actual);
        }
    }

    @Test
    void whenClientThrowsClientException_thenReturnsNull() throws Exception {
        String fakeHtml   = "<p>oops</p>";
        String fakePrompt = "P:";

        try (MockedConstruction<Client> ignored = mockConstruction(Client.class, (mockClient, ctx) -> {

            Models mockModels = mock(Models.class);
            Field f = Client.class.getField("models");
            f.setAccessible(true);
            f.set(mockClient, mockModels);

            when(mockModels.generateContent(anyString(), anyString(), any()))
                    .thenThrow(new ClientException(400, "fail", "API down"));
        })) {
            String result = ApiCaller.makeApiCall(fakeHtml, fakePrompt);
            assertNull(result);
        }
    }

    @Test
    void whenUnexpectedException_thenReturnsNull() throws Exception {
        String fakeHtml   = "";
        String fakePrompt = "";

        try (MockedConstruction<Client> ignored = mockConstruction(Client.class, (mockClient, ctx) -> {
        })) {
            String result = ApiCaller.makeApiCall(fakeHtml, fakePrompt);
            assertNull(result);
        }
    }
}