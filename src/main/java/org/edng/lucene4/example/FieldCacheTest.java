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

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 1/30/15.
 */
public class FieldCacheTest {

    @org.junit.Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        StringField stringField = new StringField("name", "", Field.Store.YES);

        String[] contents = {"alpha", "bravo", "charlie", "delta", "echo", "foxtrot"};
        for (String content : contents) {
            stringField.setStringValue(content);
            doc.removeField("name");
            doc.add(stringField);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);

        BinaryDocValues cache = FieldCache.DEFAULT.getTerms(SlowCompositeReaderWrapper.wrap(indexReader), "name", true);

        for (int i = 0; i < indexReader.maxDoc(); i++) {
            BytesRef bytesRef = cache.get(i);
            System.out.println(i + ": " + bytesRef.utf8ToString());
            switch (i) {
                case 0:
                    assertEquals("Result doesn't match on position " + i, "alpha", bytesRef.utf8ToString());
                    break;
                case 1:
                    assertEquals("Result doesn't match on position " + i, "bravo", bytesRef.utf8ToString());
                    break;
                case 2:
                    assertEquals("Result doesn't match on position " + i, "charlie", bytesRef.utf8ToString());
                    break;
                case 3:
                    assertEquals("Result doesn't match on position " + i, "delta", bytesRef.utf8ToString());
                    break;
                case 4:
                    assertEquals("Result doesn't match on position " + i, "echo", bytesRef.utf8ToString());
                    break;
                case 5:
                    assertEquals("Result doesn't match on position " + i, "foxtrot", bytesRef.utf8ToString());
                    break;
            }
        }

    }
}
