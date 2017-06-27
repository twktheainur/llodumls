package io.github.twktheainur.matching;

import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.measures.tverski.TverskiIndexSimilarityMeasureBuilder;
import org.getalp.lexsema.similarity.signatures.DefaultSemanticSignatureFactory;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class TverskiTermSimilarityRanker implements PooledTermSimilarityRanker {

    private final JedisPool jedisPool;
    private static final String SCORE_PREFIX = "rscore_";

    private static final Logger logger = LoggerFactory.getLogger(TverskiTermSimilarityRanker.class);
    private final ExecutorService threadPool;

    public TverskiTermSimilarityRanker(final JedisPool jedisPool) {
        logger.info("Initializing similarity ranker...");
        this.jedisPool = jedisPool;
        final Runtime runtime = Runtime.getRuntime();
        final int nbThreads = runtime.availableProcessors();
        threadPool = Executors.newFixedThreadPool(nbThreads);
    }

    private static final double RATIO_PROPORTION = 0.5d;

    @SuppressWarnings("FeatureEnvy")
    @Override
    public void rankBySimilarity(final List<CUITerm> cuiTermList, final String conceptDescription) {
        @SuppressWarnings("all")
        final SimilarityMeasure similarityMeasure = new TverskiIndexSimilarityMeasureBuilder()
                .alpha(1d).beta(RATIO_PROPORTION).gamma(RATIO_PROPORTION)
                .computeRatio(true).fuzzyMatching(true).regularizeOverlapInput(true).normalize(true).build();

        final SemanticSignature conceptSemanticSignature =
                DefaultSemanticSignatureFactory.DEFAULT.createSemanticSignature(conceptDescription);
        final Collection<IntermediateScorer> scorers = new ArrayList<>();

        try (Jedis jedis = jedisPool.getResource()) {
            for (final CUITerm cuiTerm : cuiTermList) {
                final String scoreString = jedis.get(SCORE_PREFIX + "_" + cuiTerm.getTerm() + "_" + conceptDescription);
                if (scoreString == null) {
                    scorers.add(new IntermediateScorer(cuiTerm, conceptSemanticSignature, similarityMeasure));
                } else {
                    cuiTerm.setScore(Double.valueOf(scoreString));
                }
            }
        }


        try {
            final List<Future<CUITerm>> intermediateScores = threadPool.invokeAll(scorers);
            try(Jedis jedis = jedisPool.getResource()) {
                for (final Future<CUITerm> intermediateScore : intermediateScores) {
                    final CUITerm cuiTerm = intermediateScore.get();
                    jedis.set(SCORE_PREFIX + "_" + cuiTerm.getTerm() + "_" + conceptDescription, String.valueOf(cuiTerm.getScore()));
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            logger.error(e.getLocalizedMessage());
        }
        cuiTermList.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
    }

    @Override
    public void release() {
        threadPool.shutdownNow();
    }

    private static final class IntermediateScorer implements Callable<CUITerm> {
        private final CUITerm cuiTerm;
        private final SemanticSignature ontologyLabel;
        private final SimilarityMeasure similarityMeasure;


        private IntermediateScorer(final CUITerm cuiTerm, final SemanticSignature ontologyLabel, final SimilarityMeasure similarityMeasure) {
            this.cuiTerm = cuiTerm;
            this.ontologyLabel = ontologyLabel;
            this.similarityMeasure = similarityMeasure;
        }

        @Override
        public CUITerm call() {
            final double score = similarityMeasure.compute(cuiTerm.getSemanticSignature(), ontologyLabel);
            cuiTerm.setScore(score);
            return cuiTerm;
        }
    }

}
