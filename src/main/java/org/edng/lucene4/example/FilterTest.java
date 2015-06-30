package org.edng.lucene4.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 12/29/14.
 */
public class FilterTest {

    @org.junit.Test
    public void runTest() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        StringField stringField = new StringField("name", "", Field.Store.YES);
        TextField textField = new TextField("content", "", Field.Store.YES);
        IntField intField = new IntField("num", 0, Field.Store.YES);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("First");
        textField.setStringValue("Humpty Dumpty sat on a wall,");
        intField.setIntValue(100);
        doc.add(stringField); doc.add(textField); doc.add(intField);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Second");
        textField.setStringValue("Humpty Dumpty had a great fall.");
        intField.setIntValue(200);
        doc.add(stringField); doc.add(textField); doc.add(intField);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Third");
        textField.setStringValue("All the king's horses and all the king's men");
        intField.setIntValue(300);
        doc.add(stringField); doc.add(textField); doc.add(intField);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Fourth");
        textField.setStringValue("Couldn't put Humpty together again.");
        intField.setIntValue(400);
        doc.add(stringField); doc.add(textField); doc.add(intField);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        TermRangeFilter termRangeFilter = TermRangeFilter.newStringRange("name", "A", "G", true, true);
        NumericRangeFilter numericRangeFilter = NumericRangeFilter.newIntRange("num", 200, 400, true, true);
        FieldCacheRangeFilter fieldCacheTermRangeFilter = FieldCacheRangeFilter.newStringRange("name", "A", "G", true, true); // single term field only because of FieldCache
        QueryWrapperFilter queryWrapperFilter = new QueryWrapperFilter(new TermQuery(new Term("content", "together")));
        PrefixFilter prefixFilter = new PrefixFilter(new Term("name", "F"));
        FieldCacheTermsFilter fieldCacheTermsFilter = new FieldCacheTermsFilter("name", "First"); // single term field only because of FieldCache
        FieldValueFilter fieldValueFilter = new FieldValueFilter("name1");
        CachingWrapperFilter cachingWrapperFilter = new CachingWrapperFilter(termRangeFilter);

        ChainedFilter chainedFilter = new ChainedFilter(new Filter[]{termRangeFilter, numericRangeFilter});

        Query query = new TermQuery(new Term("content", "humpty"));
        TopDocs topDocs = indexSearcher.search(query, cachingWrapperFilter, 100);
        System.out.println("Searching 'humpty'");
        assertEquals("Number of result doesn't match", 2, topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println("name: " +
                    doc.getField("name").stringValue() +
                    " - content: " +
                    doc.getField("content").stringValue() +
                    " - num: " +
                    doc.getField("num").stringValue());
        }

        indexReader.close();
    }
}
