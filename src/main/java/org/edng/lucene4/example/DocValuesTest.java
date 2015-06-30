package org.edng.lucene4.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 12/23/14.
 */
public class DocValuesTest {

    @org.junit.Test
    public void runTest() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document document = new Document();
        document.add(new SortedDocValuesField("sorted_string", new BytesRef("hello")));
        indexWriter.addDocument(document);

        document = new Document();
        document.add(new SortedDocValuesField("sorted_string", new BytesRef("world")));
        indexWriter.addDocument(document);

        indexWriter.commit();

        indexWriter.close();

        IndexReader reader = DirectoryReader.open(directory);

        document = reader.document(0);
        System.out.println("doc 0: " + document.toString());
        document = reader.document(1);
        System.out.println("doc 1: " + document.toString());


        for (AtomicReaderContext context : reader.leaves()) {
            AtomicReader atomicReader = context.reader();
            SortedDocValues sortedDocValues = DocValues.getSorted(atomicReader, "sorted_string");
            assertEquals("Count should be 2", 2, sortedDocValues.getValueCount());
            System.out.println("Value count: " + sortedDocValues.getValueCount());
            assertEquals("doc 0 sorted_string not match", "hello", sortedDocValues.get(0).utf8ToString());
            System.out.println("doc 0 sorted_string: " + sortedDocValues.get(0).utf8ToString());
            assertEquals("doc 1 sorted_string not match", "world", sortedDocValues.get(1).utf8ToString());
            System.out.println("doc 1 sorted_string: " + sortedDocValues.get(1).utf8ToString());
        }

        reader.close();
    }
}
