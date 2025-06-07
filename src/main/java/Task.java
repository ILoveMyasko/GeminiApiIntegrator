/*
 * Учебное приложение "Integrator".
 * Версия 1.0
 */

import java.io.File;
import java.util.Map;

/**
 * Интерфейс определяет методы работы с поставленной задачей на поиск
 * информации. Параметры задачи описываются в виде коллекции типа Map<K,V>, где
 * K определяет тип уникального ключа или имени параметра задачи, а V - тип
 * значения параметра.
 *
 * @author Ю.Д.Заковряшин, 2025
 * @param <K> тип ключа параметра задачи.
 * @param <V> тип значения параметра задачи.
 */
public interface Task<K, V> {

    public static final String DEFAULT_COMMENT = "Integrator, version ";

    /**
     * Метод чтения описания задачи из текстового файла.
     *
     * @param file текстовый файл с описанием параметров задачи.
     * @throws IntegratorException исключение выбрасывается в случае общей
     * ошибки при чтении файла.
     */
    void readTask(File file) throws IntegratorException;

    /**
     * Метод возвращает параметры задачи как коллекцию свойств вида "ключ",
     * "значение.
     *
     * @return коллекция типа Map<K,V>.
     * @throws IntegratorException исключение выбрасывается в случае общей
     * ошибки при чтении файла.
     */
    Map<K,V> getProperties() throws IntegratorException;

    /**
     * Метод возвращает значение параметра задачи с заданным ключём.
     *
     * @param key ключ параметра задачи.
     * @return значение параметра задачи с заданным ключём.
     * @throws IntegratorException исключение выбрасывается в случае неверно
     * заданного ключа.
     */
    V getProperty(K key) throws IntegratorException;
}
