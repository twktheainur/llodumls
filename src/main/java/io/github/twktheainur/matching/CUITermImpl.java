package io.github.twktheainur.matching;

import io.github.twktheainur.umlssql.UMLSLanguageCode;
import org.getalp.lexsema.similarity.signatures.DefaultSemanticSignatureFactory;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.getalp.lexsema.util.Language;


public class CUITermImpl implements CUITerm {

    private final String cui;
    private final String term;
    private final UMLSLanguageCode languageCode;
    private double score;

    private final SemanticSignature semanticSignature;

    public CUITermImpl(final String cui, final String term, final UMLSLanguageCode languageCode, final SemanticSignature semanticSignature) {
        this.cui = cui;
        this.term = term;
        this.languageCode = languageCode;
        this.semanticSignature = semanticSignature;
        semanticSignature.setLanguage(Language.valueOf(languageCode.name()));
    }

    @SuppressWarnings({"LocalVariableOfConcreteClass", "MethodWithMultipleReturnPoints"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CUITermImpl)) return false;

        final CUITermImpl cuiTerm = (CUITermImpl) o;

        return ((cui != null) ? cui.equals(cuiTerm.cui) : (cuiTerm.cui == null)) && (getLanguageCode() == cuiTerm.getLanguageCode());
    }

    @Override
    public int hashCode() {
        int result = (cui != null) ? cui.hashCode() : 0;
        result = (31 * result) + ((getLanguageCode() != null) ? getLanguageCode().hashCode() : 0);
        return result;
    }

    @Override
    public String getCUI() {
        return cui;
    }

    @Override
    public String getTerm() {
        return term;
    }

    @Override
    public UMLSLanguageCode getLanguageCode() {
        return languageCode;
    }

    @Override
    public SemanticSignature getSemanticSignature() {
        return semanticSignature;
    }

    @Override
    public void appendToSignature(final String text) {
        semanticSignature.appendSignature(DefaultSemanticSignatureFactory.DEFAULT.createSemanticSignature(text));
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public void setScore(final double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return String.format("%s %s",cui,term);
    }
}
