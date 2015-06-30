package org.edng.lucene4.example;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 2/21/15.
 */
public class NearRealTimeTrackingIndexWriterTest {

    @org.junit.Test
    public void runTest() throws Exception {

        File indexDir = new File("data/index");
        if (indexDir.exists()) {
            FileUtils.forceDelete(indexDir);
        }
        // open a directory
        Directory directory = FSDirectory.open(indexDir);

        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        SearcherManager searcherManager = new SearcherManager(indexWriter, true, new SearcherFactory());
        TrackingIndexWriter trackingIndexWriter = new TrackingIndexWriter(indexWriter);
        ControlledRealTimeReopenThread controlledRealTimeReopenThread = new ControlledRealTimeReopenThread(trackingIndexWriter, searcherManager, 5, 0.001f);
        controlledRealTimeReopenThread.start();

        long indexGeneration = 0;

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
            indexGeneration = trackingIndexWriter.addDocument(doc);
        }

        controlledRealTimeReopenThread.waitForGeneration(indexGeneration);

        IndexSearcher indexSearcher = searcherManager.acquire();

        QueryParser queryParser = new QueryParser("content", analyzer);
        Query query = queryParser.parse("humpty dumpty");

        TopDocs topDocs = indexSearcher.search(query, 100);
        assertEquals("Result doesn't match", 3, topDocs.scoreDocs.length);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println(scoreDoc.score + ": " + doc.getField("content").stringValue());
        }

        indexWriter.commit();

        System.out.println("*********");

        textField.setStringValue("humpty humpty humpty");
        doc.removeField("content");
        doc.add(textField);
        indexGeneration = trackingIndexWriter.addDocument(doc);

        controlledRealTimeReopenThread.waitForGeneration(indexGeneration);
        indexSearcher = searcherManager.acquire();

        query = queryParser.parse("humpty");
        topDocs = indexSearcher.search(query, 100);
        assertEquals("Result doesn't match", 4, topDocs.scoreDocs.length);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println(scoreDoc.score + ": " + doc.getField("content").stringValue());
        }

        controlledRealTimeReopenThread.close();
        indexWriter.commit();
    }
}
