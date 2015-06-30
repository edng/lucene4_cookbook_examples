package org.edng.lucene4.example;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 1/20/15.
 */
public class PayloadFilter extends TokenFilter {
    PayloadAttribute payloadAtt = addAttribute(PayloadAttribute.class);
    CharTermAttribute charTermAtt = addAttribute(CharTermAttribute.class);

    protected PayloadFilter(TokenStream input) {
        super(input);
    }

    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) { return false; }
        payloadAtt.setPayload(new BytesRef(
                PayloadHelper.encodeFloat(
                        determinePayload(
                                charTermAtt.toString()))));
        return true;
    }

    protected float determinePayload(String term) {
        float score = 1f;
        for (char c : term.toCharArray()) {
            switch (c) {
                case 'a':
                    score += 0.1f;
                case 'e':
                    score += 0.2f;
                case 'i':
                    score += 0.4f;
                case 'o':
                    score += 0.8f;
                case 'u':
                    score += 1.6f;
                    break;
            }
        }
        return score;
    }
}
