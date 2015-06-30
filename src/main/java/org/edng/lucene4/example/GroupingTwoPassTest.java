package org.edng.lucene4.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 3/28/15.
 */
public class GroupingTwoPassTest {

    @org.junit.Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new StringField("BookId", "B1", Field.Store.YES));
        doc.add(new StringField("Category", "Cat 1", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("BookId", "B2", Field.Store.YES));
        doc.add(new StringField("Category", "Cat 1", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("BookId", "B3", Field.Store.YES));
        doc.add(new StringField("Category", "Cat 2", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.commit();

        GroupingSearch groupingSearch = new GroupingSearch("Category");
        groupingSearch.setAllGroups(true);
        groupingSearch.setGroupDocsLimit(10);

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        TopGroups topGroups = groupingSearch.search(indexSearcher, new MatchAllDocsQuery(), 0, 10);

        assertEquals("Total group count not match", 2, topGroups.totalGroupCount.longValue());
        System.out.println("Total group count: " + topGroups.totalGroupCount);
        assertEquals("Total group hit count not match", 3, topGroups.totalGroupedHitCount);
        System.out.println("Total group hit count: " + topGroups.totalGroupedHitCount);

        for (GroupDocs groupDocs : topGroups.groups) {
            System.out.println("Group: " + ((BytesRef)groupDocs.groupValue).utf8ToString());
            for (ScoreDoc scoreDoc : groupDocs.scoreDocs) {
                doc = indexSearcher.doc(scoreDoc.doc);
                System.out.println("Category: " + doc.getField("Category").stringValue() + ", BookId: " + doc.getField("BookId").stringValue());
            }
        }
    }
}

