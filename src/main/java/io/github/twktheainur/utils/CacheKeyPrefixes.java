package io.github.twktheainur.utils;


public enum CacheKeyPrefixes {
    CUI("c_"),
    TUI("t_"),
    ALTCUI("ac_"),
    PREFLABEL("pl_"),
    MAPPING("m_"),
    CLASS_RELATION("cr_");

    private final String prefix;

    CacheKeyPrefixes(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }
}
