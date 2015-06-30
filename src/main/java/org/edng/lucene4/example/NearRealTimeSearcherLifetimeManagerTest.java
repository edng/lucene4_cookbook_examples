package org.edng.lucene4.example;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
public class NearRealTimeSearcherLifetimeManagerTest {

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

        DirectoryReader directoryReader = DirectoryReader.open(indexWriter, true);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);

        SearcherLifetimeManager searcherLifetimeManager = new SearcherLifetimeManager();
        long searcherToken = searcherLifetimeManager.record(indexSearcher);

        QueryParser queryParser = new QueryParser("content", analyzer);
        Query query = queryParser.parse("humpty dumpty");

        indexSearcher = searcherLifetimeManager.acquire(searcherToken);

        if (indexSearcher != null) {
            try {
                TopDocs topDocs = indexSearcher.search(query, 100);
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    doc = indexSearcher.doc(scoreDoc.doc);
                    if (scoreDoc.equals(topDocs.scoreDocs[0])) {
                        assertEquals("Rank 1 score not match", 0.81, scoreDoc.score, 0.1);
                    }
                    System.out.println(scoreDoc.score + ": " + doc.getField("content").stringValue());
                }
            } finally {
                searcherLifetimeManager.release(indexSearcher);
                indexSearcher = null;
            }
        } else {
            // searcher was pruned, notify user that search session has timed out
        }

        searcherLifetimeManager.prune(new SearcherLifetimeManager.PruneByAge(600.0));

        indexWriter.commit();

    }
}
