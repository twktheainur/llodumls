package io.github.twktheainur.umlssql;

import io.github.twktheainur.matching.CUITerm;
import io.github.twktheainur.matching.CUITermImpl;
import io.github.twktheainur.utils.EmptyResultsCache;
import org.apache.commons.dbcp2.BasicDataSource;
import org.getalp.lexsema.similarity.signatures.DefaultSemanticSignatureFactory;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;


public class SQLUMLSDelegate implements UMLSDelegate {

    private static final String CONCEPT_CODE_PREFIX = "code_";
    private static final String CONCEPT_NAME_MAP_PREFIX = "concept_name_map";
    private static final String ERROR_MESSAGE_CANNOT_RUN_SQL_QUERY = "Cannot run SQL query: {}";
    @SuppressWarnings("resource")
    private final BasicDataSource dataSource = new BasicDataSource();
    private final JedisPool jedisPool;
    private static final Logger logger = LoggerFactory.getLogger(SQLUMLSDelegate.class);
    private static final String CUITUI_PREFIX = "cuitui_";


    public SQLUMLSDelegate(final String jdbcURI, final String sqlUser, final String sqlPass, final String sqlDB, final JedisPool jedisPool) {
        logger.info("Initializing UMLS SQL Interface...");
        //dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(jdbcURI);
        dataSource.setUsername(sqlUser);
        dataSource.setPassword(sqlPass);
        dataSource.setDefaultCatalog(sqlDB);
        this.jedisPool = jedisPool;
    }

    @SuppressWarnings({"OverlyNestedMethod", "SynchronizeOnNonFinalField"})
    @Override
    public Collection<String> getTUIsForCUIs(final Collection<String> cuis) {
        final Collection<String> tuis = new ArrayList<>();

        for (final String cui : cuis) {
            try (Jedis jedis = jedisPool.getResource()) {
                final String key = CUITUI_PREFIX + cui;
                Collection<String> localTuis = jedis.lrange(key, 0, -1);
                if (localTuis.isEmpty() && !EmptyResultsCache.isEmpty(key, jedis)) {
                    synchronized (dataSource) {
                        try (final Connection connection = dataSource.getConnection()) {
                            try (final PreparedStatement statement = connection.prepareStatement("SELECT DISTINCT TUI FROM MRSTY WHERE CUI=?")) {
                                statement.setString(1, cui);
                                localTuis = fetchPushListPreparedStatement(statement, key, jedis);
                            } catch (final SQLException e) {
                                logger.error(ERROR_MESSAGE_CANNOT_RUN_SQL_QUERY, e.getLocalizedMessage());
                            }
                        } catch (final SQLException e) {
                            logger.error(e.getLocalizedMessage());
                        }
                    }

                }
                tuis.addAll(localTuis);
            }
        }
        return tuis;
    }

    private Collection<String> fetchPushListPreparedStatement(final PreparedStatement preparedStatement, final String cacheKey, final JedisCommands jedis) throws SQLException {
        final Collection<String> collection = new ArrayList<>();
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                collection.add(resultSet.getString(1));
            }
            if (collection.isEmpty()) {
                EmptyResultsCache.markEmpty(cacheKey, jedis);
            } else {
                jedis.lpush(cacheKey, collection.toArray(new String[collection.size()]));
            }
        }
        return collection;
    }

    @Override
    public List<CUITerm> getCUIConceptNameMap(final UMLSLanguageCode languageCode) {
        return getCUIConceptNameMap(languageCode, null);
    }

    private String generateCUIString(final Collection<String> cuis) {
        final Stream<String> stream = cuis.stream();
        final Optional<String> reduce = stream.reduce(String::concat);
        return reduce.orElse("");
    }

    private String buildCUIDescriptionQuery(final int numberOfCUIs) {
        final StringBuilder query = new StringBuilder("SELECT DISTINCT CUI,STR FROM MRCONSO WHERE LAT=?");
        query.append(" AND (");
        boolean first = true;
        for (int i = 0; i < numberOfCUIs; i++) {
            if (first) {
                first = false;
            } else {
                query.append("OR ");
            }
            query.append("CUI=? ");
        }
        query.append(");");
        return query.toString();
    }

    @SuppressWarnings({"OverlyNestedMethod", "OverlyLongMethod", "OverlyComplexMethod"})
    @Override
    public List<CUITerm> getCUIConceptNameMap(final UMLSLanguageCode languageCode, final Collection<String> cuis) {
        Map<String, String> conceptNameMap;
        try (Jedis jedis = jedisPool.getResource()) {
            final String code = languageCode.getLanguageCode();
            String key = CONCEPT_NAME_MAP_PREFIX + languageCode.getLanguageCode();
            if (cuis != null) {
                key += generateCUIString(cuis);
            }
            conceptNameMap = jedis.hgetAll(key);
            if (conceptNameMap.isEmpty() && !EmptyResultsCache.isEmpty(key, jedis)) {
                if (cuis != null) {
                    final String query = buildCUIDescriptionQuery(cuis.size());
                    synchronized (dataSource) {
                        try (final Connection connection = dataSource.getConnection()) {
                            try (final PreparedStatement statement = connection.prepareStatement(query)) {
                                logger.debug(query);
                                statement.setString(1, code);
                                int i = 2;
                                for (final String cui : cuis) {
                                    statement.setString(i, cui);
                                    i++;
                                }
                                try (final ResultSet resultSet = statement.executeQuery()) {
                                    conceptNameMap = new HashMap<>();
                                    while (resultSet.next()) {
                                        final String value = resultSet.getString(2);
                                        conceptNameMap.put(value, resultSet.getString(1));
                                    }
                                    if (conceptNameMap.isEmpty()) {
                                        EmptyResultsCache.markEmpty(key, jedis);
                                    } else {
                                        jedis.hmset(key, conceptNameMap);
                                    }
                                }
                            } catch (final SQLException e) {
                                logger.error(ERROR_MESSAGE_CANNOT_RUN_SQL_QUERY, e.getLocalizedMessage());
                            }
                        } catch (final SQLException e) {
                            logger.error(e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
        final List<CUITerm> cuiTerms = new ArrayList<>();
        populateTermList(cuiTerms, conceptNameMap, languageCode);
        return cuiTerms;
    }

    @SuppressWarnings({"OverlyNestedMethod", "OverlyLongMethod"})
    @Override
    public Collection<String> getUMLSCUIs(final String code, final UMLSLanguageCode languageCode) {
        Collection<String> codes;
        try(Jedis jedis = jedisPool.getResource()) {
            final String key = CONCEPT_CODE_PREFIX + code;
            codes = jedis.lrange(key, 0, -1);
            if (codes.isEmpty() && !EmptyResultsCache.isEmpty(key, jedis)) {
                //noinspection SynchronizeOnNonFinalField
                synchronized (dataSource) {
                    try (final Connection connection = dataSource.getConnection()) {
                        try (final PreparedStatement statement = connection.prepareStatement("SELECT DISTINCT CUI FROM MRCONSO WHERE CODE = ? AND LAT= ?")) {
                            statement.setString(1, code);
                            statement.setString(2, languageCode.getLanguageCode());
                            codes = fetchPushListPreparedStatement(statement, key,jedis);
                        } catch (final SQLException e) {
                            logger.error(ERROR_MESSAGE_CANNOT_RUN_SQL_QUERY, e.getLocalizedMessage());
                        }
                    } catch (final SQLException e) {
                        logger.error(e.getLocalizedMessage());
                    }
                }
            }
        }
        return codes;
    }

    @SuppressWarnings({"OverlyNestedMethod", "OverlyLongMethod"})
    @Override
    public Collection<String> getUMLSCUIs(final String code) {
        Collection<String> codes;
        try(Jedis jedis = jedisPool.getResource()) {
            final String key = CONCEPT_CODE_PREFIX + code;
            codes = jedis.lrange(key, 0, -1);
            if (codes.isEmpty() && !EmptyResultsCache.isEmpty(key, jedis)) {
                //noinspection SynchronizeOnNonFinalField
                synchronized (dataSource) {
                    try (final Connection connection = dataSource.getConnection()) {
                        try (final PreparedStatement statement = connection.prepareStatement("SELECT DISTINCT CUI FROM MRCONSO WHERE CODE = ?")) {
                            statement.setString(1, code);
                            codes = fetchPushListPreparedStatement(statement, key,jedis);
                        } catch (final SQLException e) {
                            logger.error(ERROR_MESSAGE_CANNOT_RUN_SQL_QUERY, e.getLocalizedMessage());
                        }
                    } catch (final SQLException e) {
                        logger.error(e.getLocalizedMessage());
                    }
                }
            }
        }
        return codes;
    }


    private void populateTermList(final List<CUITerm> cuiTerms, final Map<String, String> conceptNameMap, final UMLSLanguageCode languageCode) {
        for (final Map.Entry<String, String> entry : conceptNameMap.entrySet()) {
            final SemanticSignature semanticSignature =
                    DefaultSemanticSignatureFactory.DEFAULT.createSemanticSignature(entry.getKey());
            final CUITerm cuiTerm = new CUITermImpl(entry.getValue(), entry.getKey(), languageCode, semanticSignature);
            if (cuiTerms.contains(cuiTerm)) {
                final CUITerm other = cuiTerms.get(cuiTerms.indexOf(cuiTerm));
                other.appendToSignature(cuiTerm.getTerm());
            } else {
                cuiTerms.add(cuiTerm);
            }
        }
    }
}
