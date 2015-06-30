package org.edng.lucene4.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
/**
 * Created by ed on 1/30/15.
 */
public class CollectorTest {

    public static class MyCollector extends Collector {
        private int totalHits = 0;
        private int docBase;
        private Scorer scorer;
        private List<ScoreDoc> topDocs = new ArrayList();
        private ScoreDoc[] scoreDocs;

        public MyCollector() {
        }

        public void setScorer(Scorer scorer) {
            this.scorer = scorer;
        }

        public boolean acceptsDocsOutOfOrder() {
            return false;
        }

        public void collect(int doc) throws IOException {
            float score = scorer.score();
            if (score > 0) {
                score += (1 / (doc + 1));
            }
            ScoreDoc scoreDoc = new ScoreDoc(doc + docBase, score);
            topDocs.add(scoreDoc);
            totalHits++;
        }

        public void setNextReader(AtomicReaderContext context) {
            this.docBase = context.docBase;
        }

        public int getTotalHits() {
            return totalHits;
        }

        public ScoreDoc[] getScoreDocs() {
            if (scoreDocs != null) {
                return scoreDocs;
            }
            Collections.sort(topDocs, new Comparator<ScoreDoc>() {
                public int compare(ScoreDoc d1, ScoreDoc d2) {
                    if (d1.score > d2.score) {
                        return -1;
                    } else if (d1.score == d2.score) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
            scoreDocs = topDocs.toArray(new ScoreDoc[topDocs.size()]);
            return scoreDocs;
        }
    }

    @org.junit.Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
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
        QueryParser queryParser = new QueryParser("content", analyzer);
        Query query = queryParser.parse("humpty together");

        CollectorTest.MyCollector collector = new CollectorTest.MyCollector();
        indexSearcher.search(query, collector);

        assertEquals("Total should be 3", 3, collector.getTotalHits());

        System.out.println(collector.getTotalHits());

        ScoreDoc[] scoreDocs = collector.getScoreDocs();
        for (ScoreDoc scoreDoc : scoreDocs) {
            System.out.println("doc " + scoreDoc.doc + ", " + scoreDoc.score);
        }
    }
}
