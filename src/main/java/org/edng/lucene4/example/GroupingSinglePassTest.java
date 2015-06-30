package org.edng.lucene4.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.grouping.BlockGroupingCollector;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 3/28/15.
 */
public class GroupingSinglePassTest {

    @org.junit.Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        FieldType groupEndFieldType = new FieldType();
        groupEndFieldType.setStored(false);
        groupEndFieldType.setTokenized(false);
        groupEndFieldType.setIndexed(true);
        groupEndFieldType.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
        groupEndFieldType.setOmitNorms(true);
        Field groupEndField = new Field("groupEnd", "x", groupEndFieldType);

        List<Document> documentList = new ArrayList();
        Document doc = new Document();
        doc.add(new StringField("BookId", "B1", Field.Store.YES));
        doc.add(new StringField("Category", "Cat 1", Field.Store.YES));
        documentList.add(doc);
        doc = new Document();
        doc.add(new StringField("BookId", "B2", Field.Store.YES));
        doc.add(new StringField("Category", "Cat 1", Field.Store.YES));
        documentList.add(doc);
        doc.add(groupEndField);
        indexWriter.addDocuments(documentList);

        documentList = new ArrayList();
        doc = new Document();
        doc.add(new StringField("BookId", "B3", Field.Store.YES));
        doc.add(new StringField("Category", "Cat 2", Field.Store.YES));
        documentList.add(doc);
        doc.add(groupEndField);
        indexWriter.addDocuments(documentList);

        indexWriter.commit();

        Filter groupEndDocs = new CachingWrapperFilter(new QueryWrapperFilter(new TermQuery(new Term("groupEnd", "x"))));

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        BlockGroupingCollector blockGroupingCollector = new BlockGroupingCollector(Sort.RELEVANCE, 10, true, groupEndDocs);
        indexSearcher.search(new MatchAllDocsQuery(), null, blockGroupingCollector);

        TopGroups topGroups = blockGroupingCollector.getTopGroups(Sort.RELEVANCE, 0, 0, 10, true);

        assertEquals("Total group count not match", 2, topGroups.totalGroupCount.longValue());
        System.out.println("Total group count: " + topGroups.totalGroupCount);
        assertEquals("Total group hit count not match", 3, topGroups.totalGroupedHitCount);
        System.out.println("Total group hit count: " + topGroups.totalGroupedHitCount);

        for (GroupDocs groupDocs : topGroups.groups) {
            System.out.println("Group: " + groupDocs.groupValue);
            for (ScoreDoc scoreDoc : groupDocs.scoreDocs) {
                doc = indexSearcher.doc(scoreDoc.doc);
                System.out.println("Category: " + doc.getField("Category").stringValue() + ", BookId: " + doc.getField("BookId").stringValue());
            }
        }

    }
}

