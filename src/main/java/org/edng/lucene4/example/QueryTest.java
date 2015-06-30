package org.edng.lucene4.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 1/2/15.
 */
public class QueryTest {

    @org.junit.Test
    public void runTest() throws Exception {

        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        FieldType textFieldType = new FieldType();
        textFieldType.setIndexed(true);
        textFieldType.setTokenized(true);
        textFieldType.setStored(true);
        textFieldType.setStoreTermVectors(true);
        StringField stringField = new StringField("name", "", Field.Store.YES);
        Field textField = new Field("content", "", textFieldType);
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
        QueryParser queryParser = new QueryParser("content", analyzer);
        // configure queryParser here
        //Query query = queryParser.parse("(humpty AND dumpty) OR wall NOT sat");
        //Query query = queryParser.parse("hum*pty*");

        //FuzzyQuery query = new FuzzyQuery(new Term("content", "humpXX"));

        //Query query = NumericRangeQuery.newIntRange("num", 0, 200, true, true);

        //Query query = queryParser.parse("(name:First | name:Fourth | content:dumpty)~0.1");

        //RegexpQuery query = new RegexpQuery(new Term("content", ".um.*"));

        SpanNearQuery query1 = new SpanNearQuery(
            new SpanQuery[] {
                new SpanTermQuery(new Term("content", "wall")),
                new SpanTermQuery(new Term("content", "humpty")),
            },
            4,
            false);

        SpanFirstQuery query2 = new SpanFirstQuery(
            new SpanTermQuery(new Term("content", "sat")),
            3
        );

        SpanNotQuery query3 = new SpanNotQuery(
            query1,
            new SpanTermQuery(new Term("content", "sat"))
        );

        SpanOrQuery query4 = new SpanOrQuery(
            query1,
            new SpanTermQuery(new Term("content", "together"))
        );

        WildcardQuery wildcard = new WildcardQuery(new Term("content", "hum*"));
        SpanQuery query5 = new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard);

        SpanQuery q1  = new SpanTermQuery(new Term("content", "dumpty"));
        SpanQuery q2  = new SpanTermQuery(new Term("content2", "humpty"));
        SpanQuery maskedQuery = new FieldMaskingSpanQuery(q2, "content");
        Query query6 = new SpanNearQuery(new SpanQuery[]{q1, maskedQuery}, 4, false);

        SpanPositionRangeQuery query7 = new SpanPositionRangeQuery(
                new SpanTermQuery(new Term("content", "wall")), 5, 6);

        AnagramQuery query8 = new AnagramQuery(new TermQuery(new Term("content", "sat")), "content");

        Query query = query8;
        /*
        PhraseQuery query = new PhraseQuery();
        query.add(new Term("content", "humpty"));
        query.add(new Term("content", "wall"));
        query.setSlop(4);

        /*
        PhraseQuery phraseQuery = new PhraseQuery();
        phraseQuery.add(new Term("content", "humpty"));
        phraseQuery.add(new Term("content", "together"));

        DisjunctionMaxQuery query = new DisjunctionMaxQuery(0.1f);
        query.add(new TermQuery(new Term("name", "First")));
        query.add(phraseQuery);
        */

        /*
        BooleanQuery query = new BooleanQuery();
        query.add(new BooleanClause(
                new TermQuery(new Term("content", "humpty")),
                BooleanClause.Occur.MUST));
        query.add(new BooleanClause(new TermQuery(
                new Term("content", "dumpty")),
                BooleanClause.Occur.MUST));
        query.add(new BooleanClause(new TermQuery(
                new Term("content", "wall")),
                BooleanClause.Occur.SHOULD));
        query.add(new BooleanClause(new TermQuery(
                new Term("content", "sat")),
                BooleanClause.Occur.MUST_NOT));
                */

        /*
        PhraseQuery query = new PhraseQuery();
        query.add(new Term("content", "humpty"));
        query.add(new Term("content", "together"));

        MultiPhraseQuery query2 = new MultiPhraseQuery();
        Term[] terms1 = new Term[1];
        terms1[0] = new Term("content", "humpty");
        Term[] terms2 = new Term[2];
        terms2[0] = new Term("content", "dumpty");
        terms2[1] = new Term("content", "together");
        query2.add(terms1);
        query2.add(terms2);
        */

//        PrefixQuery query = new PrefixQuery(new Term("content", "hum"));
//        WildcardQuery query2 = new WildcardQuery(new Term("content", "*um*"));

//        Query query = new TermQuery(new Term("content", "humpty"));
//        query = new TermRangeQuery("content", new BytesRef("a"), new BytesRef("c"), true, true);
        //System.out.println(query2.getClass().getSimpleName());
        //System.out.println(query2);

        System.out.println(query.getClass().getSimpleName());
        System.out.println(query);
        TopDocs topDocs = indexSearcher.search(query, 100);

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

        queryParser.setAllowLeadingWildcard(true);
        queryParser.setAnalyzeRangeTerms(true);
        queryParser.setAutoGeneratePhraseQueries(true);
        queryParser.setDateResolution(null);
        queryParser.setDefaultOperator(QueryParser.Operator.AND);
        queryParser.setEnablePositionIncrements(true);
        queryParser.setFuzzyMinSim(1f);
        queryParser.setFuzzyPrefixLength(1);
        queryParser.setLocale(Locale.US);
        queryParser.setLowercaseExpandedTerms(true);
        queryParser.setMultiTermRewriteMethod(null);
        queryParser.setPhraseSlop(1);
        queryParser.setTimeZone(TimeZone.getTimeZone("America/New_York"));


    }
}