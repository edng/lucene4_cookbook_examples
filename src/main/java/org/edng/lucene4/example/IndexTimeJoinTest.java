package org.edng.lucene4.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.join.FixedBitSetCachingWrapperFilter;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.join.ToParentBlockJoinCollector;
import org.apache.lucene.search.join.ToParentBlockJoinQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 3/24/15.
 */
public class IndexTimeJoinTest {

    @org.junit.Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        List<Document> documentList = new ArrayList();

        Document childDoc1 = new Document();
        childDoc1.add(new StringField("name", "Child doc 1", Field.Store.YES));
        childDoc1.add(new StringField("type", "child", Field.Store.YES));
        childDoc1.add(new LongField("points", 10, Field.Store.YES));
        Document childDoc2 = new Document();
        childDoc2.add(new StringField("name", "Child doc 2", Field.Store.YES));
        childDoc2.add(new StringField("type", "child", Field.Store.YES));
        childDoc2.add(new LongField("points", 100, Field.Store.YES));
        Document parentDoc = new Document();
        parentDoc.add(new StringField("name", "Parent doc 1", Field.Store.YES));
        parentDoc.add(new StringField("type", "parent", Field.Store.YES));
        parentDoc.add(new LongField("points", 1000, Field.Store.YES));

        documentList.add(childDoc1);
        documentList.add(childDoc2);
        documentList.add(parentDoc);
        indexWriter.addDocuments(documentList);
        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query childQuery = new TermQuery(new Term("type", "child"));
        Filter parentFilter = new FixedBitSetCachingWrapperFilter(new QueryWrapperFilter(new TermQuery(new Term("type", "parent"))));

        ToParentBlockJoinQuery toParentBlockJoinQuery = new ToParentBlockJoinQuery(childQuery, parentFilter, ScoreMode.Max);
        ToParentBlockJoinCollector toParentBlockJoinCollector = new ToParentBlockJoinCollector(Sort.RELEVANCE, 10, true, true);

        indexSearcher.search(toParentBlockJoinQuery, toParentBlockJoinCollector);

        TopGroups topGroups = toParentBlockJoinCollector.getTopGroupsWithAllChildDocs(toParentBlockJoinQuery, Sort.RELEVANCE, 0, 0, true);

        assertEquals("Total group count not match", 1, topGroups.totalGroupCount.intValue());
        System.out.println("Total group count: " + topGroups.totalGroupCount);
        assertEquals("Total hits not match", 2, topGroups.totalGroupedHitCount);
        System.out.println("Total hits: " + topGroups.totalGroupedHitCount);
        Document doc = null;

        for (GroupDocs groupDocs : topGroups.groups) {
            doc = indexSearcher.doc((Integer)groupDocs.groupValue);
            System.out.println("parent: " + doc.getField("name").stringValue());
            for (ScoreDoc scoreDoc : groupDocs.scoreDocs) {
                doc = indexSearcher.doc(scoreDoc.doc);
                System.out.println(scoreDoc.score + ": " + doc.getField("name").stringValue());
            }
        }
    }
}
