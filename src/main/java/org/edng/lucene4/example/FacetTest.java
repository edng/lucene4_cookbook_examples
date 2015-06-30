package org.edng.lucene4.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 3/27/15.
 */
public class FacetTest {

    @org.junit.Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory indexDirectory = new RAMDirectory();
        Directory facetDirectory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(indexDirectory, config);
        DirectoryTaxonomyWriter directoryTaxonomyWriter = new DirectoryTaxonomyWriter(facetDirectory);
        FacetsConfig facetsConfig = new FacetsConfig();

        Document doc = new Document();
        doc.add(new StringField("BookId", "B1", Field.Store.YES));
        doc.add(new FacetField("Author", "Author 1"));
        doc.add(new FacetField("Category", "Cat 1"));
        indexWriter.addDocument(facetsConfig.build(directoryTaxonomyWriter, doc));
        doc = new Document();
        doc.add(new StringField("BookId", "B2", Field.Store.YES));
        doc.add(new FacetField("Author", "Author 2"));
        doc.add(new FacetField("Category", "Cat 1"));
        indexWriter.addDocument(facetsConfig.build(directoryTaxonomyWriter, doc));
        doc = new Document();
        doc.add(new StringField("BookId", "B3", Field.Store.YES));
        doc.add(new FacetField("Author", "Author 3"));
        doc.add(new FacetField("Category", "Cat 2"));
        indexWriter.addDocument(facetsConfig.build(directoryTaxonomyWriter, doc));

        indexWriter.commit();
        directoryTaxonomyWriter.commit();

        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        DirectoryTaxonomyReader directoryTaxonomyReader = new DirectoryTaxonomyReader(facetDirectory);
        FacetsCollector facetsCollector = new FacetsCollector();

        FacetsCollector.search(indexSearcher, new MatchAllDocsQuery(), 10, facetsCollector);
        Facets facets = new FastTaxonomyFacetCounts(directoryTaxonomyReader, facetsConfig, facetsCollector);
        FacetResult facetResult = facets.getTopChildren(10, "Category");

        assertEquals("Category facet count not matched", 2, facetResult.childCount);
        for (LabelAndValue labelAndValue : facetResult.labelValues) {
            System.out.println(labelAndValue.label + ":" + labelAndValue.value);
        }

        facetResult = facets.getTopChildren(10, "Author");
        assertEquals("Author facet count not matched", 3, facetResult.childCount);
        for (LabelAndValue labelAndValue : facetResult.labelValues) {
            System.out.println(labelAndValue.label + ":" + labelAndValue.value);
        }

        DrillDownQuery drillDownQuery = new DrillDownQuery(facetsConfig);
        drillDownQuery.add("Category", "Cat 1");
        DrillSideways drillSideways = new DrillSideways(indexSearcher, facetsConfig, directoryTaxonomyReader);
        DrillSideways.DrillSidewaysResult drillSidewaysResult = drillSideways.search(drillDownQuery, 10);

        facetResult = drillSidewaysResult.facets.getTopChildren(10, "Category");
        assertEquals("DrillSideways category facet count not matched", 2, facetResult.childCount);
        for (LabelAndValue labelAndValue : facetResult.labelValues) {
            System.out.println(labelAndValue.label + ":" + labelAndValue.value);
        }

        facetResult = drillSidewaysResult.facets.getTopChildren(10, "Author");
        assertEquals("DrillSideways author facet count not matched", 2, facetResult.childCount);
        for (LabelAndValue labelAndValue : facetResult.labelValues) {
            System.out.println(labelAndValue.label + ":" + labelAndValue.value);
        }

    }
}
