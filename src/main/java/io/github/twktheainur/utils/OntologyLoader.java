package io.github.twktheainur.utils;


import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OntologyLoader {

    private static final Logger logger = LoggerFactory.getLogger(OntologyLoader.class);

    private static final Pattern URL_PATTERN = Pattern.compile("[^:]{2,6}:.*");

    public static final String TURTLE = "TURTLE";

    private OntologyLoader() {
    }

    /**
     * Load the input ontology to process in a Jena OntModel, supports local uncompressed files, bziped/gzipped files and
     * remote files over http
     */
    public static OntModel loadModel(final String modelURL) {
        final Path path = Paths.get(modelURL);
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        try {
            logger.info("Reading ontology model...");
            final Matcher matcher = URL_PATTERN.matcher(modelURL);
            if (matcher.matches()) {
                // It's an URL
                ontModel.read(modelURL);
                logger.info("\tFrom URL: {}", modelURL);
            } else if (Files.isDirectory(path)) {
                logger.info("\tFrom TDB dataset: {}", modelURL);
                final Dataset dataset = TDBFactory.createDataset(path.toString());
                dataset.begin(ReadWrite.READ);
                final Model model = dataset.getDefaultModel();
                dataset.end();
                ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, model);
            } else {
                logger.info("\tFrom File: {}", modelURL);
                // It's a file
                @SuppressWarnings("HardcodedFileSeparator") String rdfFormat = "RDF/XML";
                if (modelURL.contains(".ttl")) {
                    rdfFormat = TURTLE;
                }

                final InputStreamReader modelReader = getFileModelReader(modelURL);

                ontModel.read(modelReader, null, rdfFormat);
                modelReader.close();
            }

        } catch (final FileNotFoundException e) {
            logger.error("Could not read {}", modelURL);
            System.exit(1);
        } catch (final IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        return ontModel;
    }

    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
    private static InputStreamReader getFileModelReader(final String modelURL) throws IOException {
        final InputStreamReader modelReader;
        if (modelURL.endsWith(".bz2")) {
            modelReader = new InputStreamReader(new BZip2CompressorInputStream(new FileInputStream(modelURL)), "UTF-8");
        } else if (modelURL.endsWith(".gz")) {
            modelReader = new InputStreamReader(new GzipCompressorInputStream(new FileInputStream(modelURL)), "UTF-8");
        } else {
            modelReader = new InputStreamReader(new FileInputStream(modelURL), "UTF-8");
        }
        return modelReader;
    }
}
