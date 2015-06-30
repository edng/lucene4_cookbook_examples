package org.edng.lucene4.example;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.payloads.AveragePayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 12/24/14.
 */
public class SimilarityTest3 {

    @org.junit.Test
    public void runTest() throws Exception {

        class MySimilarity extends DefaultSimilarity {
            @Override
            public float coord(int overlap, int maxOverlap) {
                return super.coord(overlap, maxOverlap);
                /*
                if (overlap > 1) {
                    return overlap / maxOverlap;
                } else {
                    return 10;
                }
                */
            }
            @Override
            public float idf(long docFreq, long numDocs) {
                return super.idf(docFreq, numDocs);
                /*
                if (docFreq > 2) {
                    return super.idf(docFreq, numDocs);
                } else {
                    return super.idf(docFreq * 100, numDocs);
                }
                */
            }
            @Override
            public float lengthNorm(FieldInvertState state) {
                /*
                if (state.getLength() % 2 == 1) {
                    return super.lengthNorm(state) * 100;
                }
                */
                return super.lengthNorm(state);
            }
            @Override
            public float queryNorm(float sumOfSquaredWeights) {
                /*
                if (Math.round(sumOfSquaredWeights * 100f) % 2 == 0) {
                    return super.queryNorm(sumOfSquaredWeights) * 100;
                }
                */
                return super.queryNorm(sumOfSquaredWeights);
            }
            @Override
            public float scorePayload(int doc, int start, int end, BytesRef payload) {
                float val = PayloadHelper.decodeFloat(payload.bytes);
                if (start == 0 || start == 1) {
                    return val * 0.1f;
                }
                return val * 100f;
            }
            @Override
            public float sloppyFreq(int distance) {
                /*
                if (distance == 0) {
                    return super.sloppyFreq(distance) * 100;
                }
                */
                return super.sloppyFreq(distance);
            }
            @Override
            public float tf(float freq) {
                /*
                if (freq > 1f) {
                    return super.tf(freq) * 100;
                }
                */
                return super.tf(freq);
            }
        }

        PayloadAnalyzer analyzer = new PayloadAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        MySimilarity similarity = new MySimilarity();
        config.setSimilarity(similarity);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        TextField textField = new TextField("content", "", Field.Store.YES);

        String[] contents = {"Humpty Dumpty sat on a wall,",
                "Humpty Dumpty had a great fall.",
                "All the king's horses and all the king's men",
                "Couldn't put Humpty together again."};
        for (String content : contents) {
            textField.setStringValue(content);
            doc.removeField("content");
            doc.add(textField);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        indexSearcher.setSimilarity(similarity);
        QueryParser queryParser = new QueryParser("content", analyzer);
        //Query query = queryParser.parse("humpty dumpty");

        BooleanQuery query = new BooleanQuery();
        query.add(new PayloadTermQuery(
                new Term("content", "humpty"),
                new AveragePayloadFunction(), true),
                BooleanClause.Occur.SHOULD);
        query.add(new PayloadTermQuery(
                new Term("content", "dumpty"),
                new AveragePayloadFunction(), true),
                BooleanClause.Occur.SHOULD);

        TopDocs topDocs = indexSearcher.search(query, 100);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            if (scoreDoc.equals(topDocs.scoreDocs[0])) {
                assertEquals("Rank 1 score not match", 24.66, scoreDoc.score, 0.1);
            }
            System.out.println(scoreDoc.score + ": " + doc.getField("content").stringValue());
        }
    }
}
