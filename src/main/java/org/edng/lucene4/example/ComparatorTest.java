package org.edng.lucene4.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * This class tests a custom FieldComparator comparing terms by length only where
 * the results is sorted by length.
 *
 * Created by ed on 1/30/15.
 */
public class ComparatorTest {

    public static class MyFieldComparator extends FieldComparator<String> {
        private String field;
        private String bottom;
        private String topValue;
        private BinaryDocValues cache;
        private String[] values;

        public MyFieldComparator(String field, int numHits) {
            this.field = field;
            this.values = new String[numHits];
        }
        public int compare(int slot1, int slot2) {
            return compareValues(values[slot1], values[slot2]);
        }
        public int compareBottom(int doc) {
            return compareValues(bottom, cache.get(doc).utf8ToString());
        }
        public int compareTop(int doc) {
            return compareValues(topValue, cache.get(doc).utf8ToString());
        }
        public int compareValues(String first, String second) {
            int val = first.length() - second.length();
            return val == 0 ? first.compareTo(second) : val;
        }
        public void copy(int slot, int doc) {
            values[slot] = cache.get(doc).utf8ToString();
        }
        public void setBottom(int slot) {
            this.bottom = values[slot];
        }
        public void setTopValue(String value) {
            this.topValue = value;
        }
        public String value(int slot) {
            return values[slot];
        }
        public FieldComparator<String> setNextReader(AtomicReaderContext context) throws IOException {
            this.cache = FieldCache.DEFAULT.getTerms(context.reader(), "name", true);
            return this;
        }
    }
    public static class MyFieldComparatorSource extends FieldComparatorSource {
        public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed) {
            return new MyFieldComparator(fieldname, numHits);
        }
    }

    @org.junit.Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        StringField stringField = new StringField("name", "", Field.Store.YES);

        String[] contents = {"foxtrot", "echo", "delta", "charlie", "bravo", "alpha"};
        for (String content : contents) {
            stringField.setStringValue(content);
            doc.removeField("name");
            doc.add(stringField);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        WildcardQuery query = new WildcardQuery(new Term("name","*"));
        SortField sortField = new SortField("name", new MyFieldComparatorSource());
        Sort sort = new Sort(sortField);

        TopDocs topDocs = indexSearcher.search(query, null, 100, sort);
        int i = 0;
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println(scoreDoc.score + ": " + doc.getField("name").stringValue());
            if (i == 0) assertTrue("Position 0 should be 'echo' but gotten '" + doc.getField("name").stringValue() + "' instead", doc.getField("name").stringValue().equals("echo"));
            if (i == 5) assertTrue("Position 5 should be 'charlie' or 'foxtrot' but gotten '" + doc.getField("name").stringValue() + "' instead", doc.getField("name").stringValue().equals("charlie") || doc.getField("name").stringValue().equals("foxtrot"));
            i++;
        }
    }
}
