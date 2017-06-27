package io.github.twktheainur.umlssql;


import io.github.twktheainur.matching.CUITerm;

import java.util.Collection;
import java.util.List;

public interface UMLSDelegate {
    Collection<String> getTUIsForCUIs(final Collection<String> cuis);
    List<CUITerm> getCUIConceptNameMap(final UMLSLanguageCode languageCode);
    List<CUITerm> getCUIConceptNameMap(final UMLSLanguageCode languageCode, Collection<String> cuis);

    @SuppressWarnings({"OverlyNestedMethod", "OverlyLongMethod"})
    Collection<String> getUMLSCUIs(final String code, final UMLSLanguageCode languageCode);
    public Collection<String> getUMLSCUIs(final String code);
}
