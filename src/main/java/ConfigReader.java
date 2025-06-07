import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {

    private static final String filePath = "properties.cfg";

    public static Map<String, String> readSpecificProperties(String... keysToRead) {
        Map<String, String> properties = new HashMap<>();
        for (String key : keysToRead) {
            properties.put(key, null);
        }

        Path path = Paths.get(filePath);

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int separatorIndex = line.indexOf('=');
                if (separatorIndex > 0) {
                    String key = line.substring(0, separatorIndex).trim();
                    String value = line.substring(separatorIndex + 1).trim();

                    if (properties.containsKey(key)) {
                        properties.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла конфигурации '" + filePath + "': " + e.getMessage());
            for (String key : keysToRead) {
                properties.put(key, null);
            }
            return properties;
        }
        return properties;
    }
}