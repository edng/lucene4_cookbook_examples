package org.edng.lucene4.example;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;

/**
 * Created by ed on 2/2/15.
 */
public class IndexSearcherTest {
    public static void main(String[] args) throws Exception {
        Directory directory = FSDirectory.open(new File("/data/index"));
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
    }
}
