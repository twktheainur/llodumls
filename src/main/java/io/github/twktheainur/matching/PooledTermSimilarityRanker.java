package io.github.twktheainur.matching;

public interface PooledTermSimilarityRanker extends TermSimilarityRanker {
    void release();
}
