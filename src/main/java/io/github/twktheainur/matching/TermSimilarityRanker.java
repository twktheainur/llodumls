package io.github.twktheainur.matching;


import java.util.List;

@FunctionalInterface
public interface TermSimilarityRanker {
    void rankBySimilarity(List<CUITerm> cuiTermList, String conceptDescription);
}
