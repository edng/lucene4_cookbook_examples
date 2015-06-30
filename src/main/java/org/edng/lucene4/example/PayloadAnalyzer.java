package org.edng.lucene4.example;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

import java.io.Reader;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 1/20/15.
 */
public class PayloadAnalyzer extends StopwordAnalyzerBase {

    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final StandardTokenizer source = new StandardTokenizer(reader);
        TokenStream filter = new StandardFilter(source);
        filter = new LowerCaseFilter(filter);
        filter = new StopFilter(filter, stopwords);
        filter = new PayloadFilter(filter);
        return new TokenStreamComponents(source, filter);
    }
}
