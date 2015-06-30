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
public class IndexDeletionPolicyTest2 {

    @org.junit.Test
    public void runTest() throws Exception {

        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        SnapshotDeletionPolicy policy = new SnapshotDeletionPolicy(NoDeletionPolicy.INSTANCE);
        config.setIndexDeletionPolicy(policy);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        IndexCommit lastSnapshot;

        Document document = new Document();
        indexWriter.addDocument(document);
        indexWriter.commit();

        lastSnapshot = policy.snapshot();

        document = new Document();
        indexWriter.addDocument(document);
        indexWriter.commit();

        lastSnapshot = policy.snapshot();

        document = new Document();
        indexWriter.addDocument(document);
        indexWriter.rollback();

        indexWriter.close();

        List<IndexCommit> commits = DirectoryReader.listCommits(directory);
        assertEquals("Commits count doesn't match", 2, commits.size());
        System.out.println("Commits count: " + commits.size());

        for (IndexCommit commit : commits) {
            IndexReader reader = DirectoryReader.open(commit);
            System.out.println("Commit " + commit.getSegmentCount());
            System.out.println("Number of docs: " + reader.numDocs());
        }

        System.out.println("");
        assertEquals("Snapshots count doesn't match", 2, policy.getSnapshotCount());
        System.out.println("Snapshots count: " + policy.getSnapshotCount());

        List<IndexCommit> snapshots = policy.getSnapshots();

        for (IndexCommit snapshot : snapshots) {
            IndexReader reader = DirectoryReader.open(snapshot);
            System.out.println("Snapshot " + snapshot.getSegmentCount());
            System.out.println("Number of docs: " + reader.numDocs());
        }

        policy.release(lastSnapshot);

        System.out.println("");
        assertEquals("Snapshots count doesn't match", 1, policy.getSnapshotCount());
        System.out.println("Snapshots count: " + policy.getSnapshotCount());
    }
}
