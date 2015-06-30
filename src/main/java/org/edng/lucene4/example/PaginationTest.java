package org.edng.lucene4.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import static org.junit.Assert.assertTrue;

/**
 * Created by ed on 1/30/15.
 */
public class PaginationTest {

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
        Query query = queryParser.parse("humpty dumpty");

        TopDocs topDocs = indexSearcher.search(query, 2);
        ScoreDoc lastScoreDoc = null;
        int page = 1;
        System.out.println("Total hits: " + topDocs.totalHits);

        while (true) {

            assertTrue("Page number not match, got page " + page, page >= 1 && page <= 2);

            System.out.println("Page " + page);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                doc = indexReader.document(scoreDoc.doc);
                System.out.println(scoreDoc.score + ": " + doc.getField("content").stringValue());
                lastScoreDoc = scoreDoc;
            }

            topDocs = indexSearcher.searchAfter(lastScoreDoc, query, 2);

            if (topDocs.scoreDocs.length == 0) {
                break;
            }

            page++;
        }
    }
}
