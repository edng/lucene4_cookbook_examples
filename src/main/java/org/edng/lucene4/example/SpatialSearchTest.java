package org.edng.lucene4.example;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Rectangle;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.bbox.BBoxStrategy;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 3/21/15.
 */
public class SpatialSearchTest {

    @org.junit.Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();

        SpatialContext spatialContext = SpatialContext.GEO;
        BBoxStrategy bBoxStrategy = new BBoxStrategy(spatialContext, "rectangle");

        Rectangle rectangle = spatialContext.makeRectangle(1.0d, 5.0d, 1.0d, 5.0d);

        Field[] fields = bBoxStrategy.createIndexableFields(rectangle);

        for (Field field : fields) {
            doc.add(field);
        }

        doc.add(new StringField("name", "Rectangle 1", Field.Store.YES));

        indexWriter.addDocument(doc);
        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Rectangle rectangle2 = spatialContext.makeRectangle(2.0d, 4.0d, 0.0d, 2.0d);
        SpatialArgs spatialArgs = new SpatialArgs(SpatialOperation.Intersects, rectangle2);
        Query query = bBoxStrategy.makeQuery(spatialArgs);

        TopDocs topDocs = indexSearcher.search(query, 10);
        System.out.println("Total hits: " + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            assertEquals("Score not match", 1.0, scoreDoc.score, 0.1);
            System.out.println(scoreDoc.score + ": " + doc.getField("name").stringValue());
        }
    }
}
