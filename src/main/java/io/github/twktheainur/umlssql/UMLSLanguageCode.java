package io.github.twktheainur.umlssql;


public enum UMLSLanguageCode {

    ENGLISH("ENG","en"),
    FRENCH("FRE","fr"),
    CZECH("CZE","cz"),
    FINNISH("FIN","fi"),
    GERMAN("GER","de"),
    ITALIAN("ITA","it"),
    JAPANESE("JPN","jp"),
    POLISH("POL","pl"),
    PORTUGUESE("POR","pt"),
    RUSSIAN("RUS", "ru"),
    SPANISH("SPA", "es"),
    SWEDISH("SWE","sw"),
    SERBO_CROATIAN("SCR","hr"),
    DUTCH("DUT","nl"),
        LATVIAN("LAV","lv"),
    HUNGARIAN("HUN","hu"),
    KOREAN("KOR","kr"),
    DANISH("DAN","da"),
    NORWEGIAN("NOR", "no"),
    HEBREW("HEB","he"),
    BASQUE("BAQ", "eu");

    UMLSLanguageCode(final String languageCode, final String shortCode) {
        this.languageCode = languageCode;
        this.shortCode = shortCode;
    }

    private final String languageCode;
    private final String shortCode;

    @SuppressWarnings("PublicMethodNotExposedInInterface")
    public String getLanguageCode() {
        return languageCode;
    }

    @SuppressWarnings("PublicMethodNotExposedInInterface")
    public String getShortCode() {
        return shortCode;
    }
}
