package org.edng.lucene4.example;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import static org.junit.Assert.assertTrue;

/**
 * Created by ed on 4/1/15.
 */
public class HighlighterTest {

    @org.junit.Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new TextField("content", "Humpty Dumpty sat on a wall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new TextField("content", "Humpty Dumpty had a great fall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new TextField("content", "All the king's horses and all the king's men", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new TextField("content", "Couldn't put Humpty together again", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term("content", "humpty"));

        TopDocs topDocs = indexSearcher.search(query, 10);

        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<strong>", "</strong>");
        SimpleHTMLEncoder simpleHTMLEncoder = new SimpleHTMLEncoder();
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, simpleHTMLEncoder, new QueryScorer(query));

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexSearcher.doc(scoreDoc.doc);
            String text = doc.get("content");
            TokenStream tokenStream = TokenSources.getAnyTokenStream(indexReader, scoreDoc.doc, "content", analyzer);
            TextFragment[] textFragments = highlighter.getBestTextFragments(tokenStream, text, false, 10);
            for (TextFragment textFragment : textFragments) {
                if (textFragment != null && textFragment.getScore() > 0) {
                    System.out.println(textFragment.toString());
                    assertTrue("Result should contain strong tag <strong>Humpty</strong> but gotten \""+textFragment+"\" instead", textFragment.toString().indexOf("<strong>Humpty</strong>") >= 0);
                }
            }
        }

    }

}
