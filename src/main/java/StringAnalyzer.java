import org.jsoup.Jsoup;

public class StringAnalyzer implements Analyzer<String>{

    private int currentMode;

    public StringAnalyzer() {
        currentMode = Mode.FINAL;
    }
    public StringAnalyzer(int mode) {
        if (mode != Mode.ORIGINAL && mode != Mode.CLEANED &&
                mode != Mode.PURIFIED && mode != Mode.FINAL) {
            System.err.println("Предупреждение: Задан некорректный режим " + mode + ". Используется режим ORIGINAL по умолчанию.");
            this.currentMode = Mode.ORIGINAL;
        } else {
            this.currentMode = mode;
        }

    }

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

        String cleanedText = Jsoup.parse(htmlText).text();

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
