package org.edng.lucene4.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 12/24/14.
 */
public class IndexDeletionPolicyTest {

    @org.junit.Test
    public void runTest() throws Exception {

        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        config.setIndexDeletionPolicy(NoDeletionPolicy.INSTANCE);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document document = new Document();
        indexWriter.addDocument(document);
        indexWriter.commit();

        document = new Document();
        indexWriter.addDocument(document);
        indexWriter.commit();

        document = new Document();
        indexWriter.addDocument(document);
        indexWriter.rollback();

        indexWriter.close();

        List<IndexCommit> commits = DirectoryReader.listCommits(directory);

        for (IndexCommit commit : commits) {
            IndexReader reader = DirectoryReader.open(commit);
            if (commit.equals(commits.get(0))) {
                assertEquals("Commit 1 number of docs doesn't match", 1, reader.numDocs());
            } else if (commit.equals(commits.get(1))) {
                assertEquals("Commit 2 number of docs doesn't match", 2, reader.numDocs());
            }
            System.out.println("Commit " + commit.getSegmentCount());
            System.out.println("Number of docs: " + reader.numDocs());
        }
    }
}
