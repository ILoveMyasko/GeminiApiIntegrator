import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Утилитный класс для выполнения операций с файлами,
 * таких как чтение конфигурационного файла и запись/чтение текстовых файлов.
 */
public class FileHandler {

    /**
     * Путь к файлу конфигурации по умолчанию.
     */
    private static final String filePath = "properties.cfg";


    /**
     * Читает указанные ключи и их значения из файла конфигурации {@code properties.cfg}.
     * Файл должен иметь формат "ключ=значение". Строки, начинающиеся с '#',
     * и пустые строки игнорируются.
     *
     * @param keysToRead переменное число строк, представляющих ключи, значения которых необходимо прочитать.
     * @return Карта (Map), где ключами являются запрошенные {@code keysToRead},
     *         а значениями — соответствующие им строки из файла. Если ключ не найден
     *         или произошла ошибка чтения, значение для этого ключа будет {@code null}.
     */
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

    /**
     * Записывает указанное строковое содержимое в файл по заданному пути.
     * Выводит сообщение об успехе или ошибке в стандартные потоки вывода/ошибок.
     *
     * @param outputFilePath Путь к файлу, в который будет записано содержимое. Не может быть {@code null}.
     * @param content Строковое содержимое для записи. Не может быть {@code null}.
     * @throws NullPointerException если {@code outputFilePath} или {@code content} равны {@code null}.
     */
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

    /**
     * Читает все содержимое файла по указанному пути в одну строку.
     *
     * @param inputFilePath Путь к файлу для чтения. Не может быть {@code null}.
     * @return Строка, содержащая все содержимое файла.
     * @throws NullPointerException если {@code inputFilePath} равен {@code null}.
     * @throws RuntimeException если происходит ошибка ввода-вывода при чтении файла.
     *                        Исходное {@link IOException} будет являться причиной этого исключения.
     */
    public static String readFromFile(String inputFilePath) {
        if (inputFilePath == null) {
            throw new NullPointerException("Путь к файлу не может быть null.");
        }
        try {
            return Files.readString(Paths.get(inputFilePath));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла: " + inputFilePath, e);
        }
    }


}