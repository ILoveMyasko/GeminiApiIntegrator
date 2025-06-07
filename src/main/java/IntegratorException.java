/*
 * Учебное приложение "Integrator".
 * Версия 1.0
 */

/**
 * Класс представляет базовое исключение для специфичных исключений приложения.
 * @author Ю.Д.Заковряшин, 2025
 */
public class IntegratorException extends Exception{
    public IntegratorException (){
        super("Common Integrator exception");
    }

    public IntegratorException(String message) {
        super(message);
    }

    public IntegratorException(String message, Throwable cause) {
        super(message, cause);
    }
     
}
