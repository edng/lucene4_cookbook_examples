package org.edng.lucene4.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 12/24/14.
 */
public class SimilarityTest2 {

    @org.junit.Test
    public void runTest() throws Exception {

        class MySimilarity extends Similarity {
            private Similarity sim = null;
            public MySimilarity(Similarity sim) {
                this.sim = sim;
            }
            @Override
            public float coord(int overlap, int maxOverlap) {
                return sim.coord(overlap, maxOverlap);
            }
            @Override
            public float queryNorm(float valueForNormalization) {
                return sim.queryNorm(valueForNormalization);
            }
            @Override
            public long computeNorm(FieldInvertState state) {
                return sim.computeNorm(state);
            }
            @Override
            public SimWeight computeWeight(float queryBoost,
                                                      CollectionStatistics collectionStats,
                                                      TermStatistics... termStats) {
                return sim.computeWeight(queryBoost, collectionStats, termStats);
            }

            @Override
            public SimScorer simScorer(SimWeight weight,
                                                  AtomicReaderContext context)
                    throws IOException {
                final SimScorer scorer = sim.simScorer(weight, context);

                return new SimScorer() {
                    @Override
                    public float score(int i, float v) {
                        return scorer.score(i, v);
                    }

                    @Override
                    public float computeSlopFactor(int i) {
                        return scorer.computeSlopFactor(i);
                    }

                    @Override
                    public float computePayloadFactor(int i, int i1, int i2, BytesRef bytesRef) {
                        return scorer.computePayloadFactor(i, i1, i2, bytesRef);
                    }
                };
            }
        }

        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        MySimilarity similarity = new MySimilarity(new DefaultSimilarity());
        config.setSimilarity(similarity);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        TextField textField = new TextField("name", "", Field.Store.YES);
        NumericDocValuesField docValuesField = new NumericDocValuesField("ranking", 1);

        long ranking = 1l;
        String[] names = {"John R Smith", "Mary Smith", "Peter Smith"};
        for (String name : names) {
            ranking *= 2;
            textField.setStringValue(name);
            docValuesField.setLongValue(ranking);
            doc.removeField("name");
            doc.removeField("ranking");
            doc.add(textField);
            doc.add(docValuesField);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        indexSearcher.setSimilarity(similarity);
        Query query = new TermQuery(new Term("name", "smith"));
        TopDocs topDocs = indexSearcher.search(query, 100);
        System.out.println("Searching 'smith'");
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            if (scoreDoc.equals(topDocs.scoreDocs[0])) {
                assertEquals("Rank 1 not match", "Mary Smith", doc.getField("name").stringValue());
            }
            System.out.println(doc.getField("name").stringValue());
        }
    }
}
