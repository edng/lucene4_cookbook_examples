package org.edng.lucene4.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 12/29/14.
 */
public class UserSecurityFilter extends Filter {

    private String userIdField;
    private String groupIdField;
    private String userId;
    private String groupId;

    public UserSecurityFilter(String userIdField, String groupIdField, String userId, String groupId) {
        this.userIdField = userIdField;
        this.groupIdField = groupIdField;
        this.userId = userId;
        this.groupId = groupId;
    }

    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
        final SortedDocValues userIdDocValues = FieldCache.DEFAULT.getTermsIndex(context.reader(), userIdField);
        final SortedDocValues groupIdDocValues = FieldCache.DEFAULT.getTermsIndex(context.reader(), groupIdField);

        final int userIdOrd = userIdDocValues.lookupTerm(new BytesRef(userId));
        final int groupIdOrd = groupIdDocValues.lookupTerm(new BytesRef(groupId));

        return new FieldCacheDocIdSet(context.reader().maxDoc(), acceptDocs) {
            @Override
            protected final boolean matchDoc(int doc) {
                final int userIdDocOrd = userIdDocValues.getOrd(doc);
                final int groupIdDocOrd = groupIdDocValues.getOrd(doc);
                return userIdDocOrd == userIdOrd || groupIdDocOrd >= groupIdOrd;
            }
        };
    }

    // 10 - admin, 20 - manager, 30 - user, 40 - guest
    public static void main(String[] args) throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        StringField stringFieldFile = new StringField("file", "", Field.Store.YES);
        StringField stringFieldUserId = new StringField("userId", "", Field.Store.YES);
        StringField stringFieldGroupId = new StringField("groupId", "", Field.Store.YES);

        doc.removeField("file"); doc.removeField("userId"); doc.removeField("groupId");
        stringFieldFile.setStringValue("Z:\\shared\\finance\\2014-sales.xls");
        stringFieldUserId.setStringValue("1001");
        stringFieldGroupId.setStringValue("20");
        doc.add(stringFieldFile); doc.add(stringFieldUserId); doc.add(stringFieldGroupId);
        indexWriter.addDocument(doc);

        doc.removeField("file"); doc.removeField("userId"); doc.removeField("groupId");
        stringFieldFile.setStringValue("Z:\\shared\\company\\2014-policy.doc");
        stringFieldUserId.setStringValue("1101");
        stringFieldGroupId.setStringValue("30");
        doc.add(stringFieldFile); doc.add(stringFieldUserId); doc.add(stringFieldGroupId);
        indexWriter.addDocument(doc);

        doc.removeField("file"); doc.removeField("userId"); doc.removeField("groupId");
        stringFieldFile.setStringValue("Z:\\shared\\company\\2014-terms-and-conditions.doc");
        stringFieldUserId.setStringValue("1205");
        stringFieldGroupId.setStringValue("40");
        doc.add(stringFieldFile); doc.add(stringFieldUserId); doc.add(stringFieldGroupId);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        UserSecurityFilter userSecurityFilter = new UserSecurityFilter("userId", "groupId", "1001", "40");

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new MatchAllDocsQuery();
        TopDocs topDocs = indexSearcher.search(query, userSecurityFilter, 100);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println("file: " +
                    doc.getField("file").stringValue() +
                    " - userId: " +
                    doc.getField("userId").stringValue() +
                    " - groupId: " +
                    doc.getField("groupId").stringValue());
        }

        indexReader.close();
    }
}
