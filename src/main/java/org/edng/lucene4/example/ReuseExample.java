package org.edng.lucene4.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 12/24/14.
 */
public class ReuseExample {

    @org.junit.Test
    public void runTest() throws Exception {

        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        StringField stringField = new StringField("name", "", Field.Store.YES);

        String[] names = {"John", "Mary", "Peter"};
        for (String name : names) {
            stringField.setStringValue(name);
            doc.removeField("name");
            doc.add(stringField);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();
        IndexReader reader = DirectoryReader.open(directory);
        for (int i = 0; i < 3; i++) {
            doc = reader.document(i);
            switch (i) {
                case 0:
                    assertEquals("Rank " + (i+1) + " not match", "John", doc.getField("name").stringValue());
                    break;
                case 1:
                    assertEquals("Rank " + (i+1) + " not match", "Mary", doc.getField("name").stringValue());
                    break;
                case 2:
                    assertEquals("Rank " + (i+1) + " not match", "Peter", doc.getField("name").stringValue());
                    break;
            }
            System.out.println("DocId: " + i + ", name: " + doc.getField("name").stringValue());
        }
    }
}
