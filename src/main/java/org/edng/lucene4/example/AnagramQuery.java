package org.edng.lucene4.example;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.*;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a custom query class that uses AnagramQueryScoreProvider for scoring.
 *
 * Created by ed on 1/6/15.
 */
public class AnagramQuery extends CustomScoreQuery {

    private final String field;
    private final Set<String> terms = new HashSet<String>();

    public AnagramQuery(Query subquery, String field) {
        super(subquery);
        this.field = field;
        HashSet<Term> termSet = new HashSet<Term>();
        subquery.extractTerms(termSet);
        for (Term term : termSet) {
            terms.add(term.text());
        }
    }

    @Override
    protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) {
        return new AnagramQueryScoreProvider(context, field, terms);
    }
}
