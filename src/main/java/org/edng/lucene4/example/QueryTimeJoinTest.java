package org.edng.lucene4.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.join.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 3/24/15.
 */
public class QueryTimeJoinTest {

    @org.junit.Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new StringField("name", "A Book", Field.Store.YES));
        doc.add(new StringField("type", "book", Field.Store.YES));
        doc.add(new LongField("bookAuthorId", 1, Field.Store.YES));
        doc.add(new LongField("bookId", 1, Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("name", "An Author", Field.Store.YES));
        doc.add(new StringField("type", "author", Field.Store.YES));
        doc.add(new LongField("authorId", 1, Field.Store.YES));
        indexWriter.addDocument(doc);
        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        String fromField = "bookAuthorId";
        boolean multipleValuesPerDocument = false;
        String toField = "authorId";
        ScoreMode scoreMode = ScoreMode.Max;
        Query fromQuery = new TermQuery(new Term("type", "book"));

        Query joinQuery = JoinUtil.createJoinQuery(
                fromField,
                multipleValuesPerDocument,
                toField,
                fromQuery,
                indexSearcher,
                scoreMode);

        TopDocs topDocs = indexSearcher.search(joinQuery, 10);

        assertEquals("Total hits not match", 1, topDocs.totalHits);
        System.out.println("Total hits: " + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println(scoreDoc.score + ": " + doc.getField("name").stringValue());
        }
    }
}
