package io.github.twktheainur.matching;


import io.github.twktheainur.umlssql.UMLSLanguageCode;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;

public interface CUITerm {
    String getCUI();
    String getTerm();
    UMLSLanguageCode getLanguageCode();
    SemanticSignature getSemanticSignature();
    void appendToSignature(String text);
    double getScore();

    void setScore(double score);
}
