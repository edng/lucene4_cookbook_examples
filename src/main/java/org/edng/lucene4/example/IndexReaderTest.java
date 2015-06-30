package org.edng.lucene4.example;

import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 1/28/15.
 */
public class IndexReaderTest {
    public static void main(String[] args) throws Exception {

        // open a directory
        Directory directory = FSDirectory.open(new File("/data/index"));
        // set up a DirectoryReader
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        // pull a list of underlying AtomicReaders
        List<AtomicReaderContext> atomicReaderContexts = directoryReader.leaves();
        // retrieve the first AtomicReader from the list
        AtomicReader atomicReader = atomicReaderContexts.get(0).reader();
        // open another DirectoryReader by calling openIfChanged
        DirectoryReader newDirectoryReader = DirectoryReader.openIfChanged(directoryReader);
        // assign newDirectoryReader
        if (newDirectoryReader != null) {
            IndexSearcher indexSearcher = new IndexSearcher(newDirectoryReader);
            // close the old DirectoryReader
            directoryReader.close();
        }
    }
}
