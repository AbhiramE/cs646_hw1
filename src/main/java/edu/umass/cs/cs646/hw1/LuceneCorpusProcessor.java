package edu.umass.cs.cs646.hw1;

import edu.umass.cs.cs646.utils.LuceneUtils;
import edu.umass.cs.cs646.utils.SearchResult;
import javafx.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.*;;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Valar Dohaeris 9/17/16.
 */

public class LuceneCorpusProcessor {


    public LuceneCorpusProcessor() throws IOException {
    }

    public static void main(String[] args) {
        double startime = System.currentTimeMillis();

        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {

                // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
                TokenStreamComponents ts = new TokenStreamComponents(new StandardTokenizer());

                // Step 2: transforming all tokens into lowercased ones
                ts = new TokenStreamComponents(ts.getTokenizer(), new LowerCaseFilter(ts.getTokenStream()));
                return ts;
            }
        };

        // modify to your index path
        String pathIndex = "/home/abhiram/codebase/InformationRetrieval/acm_corpus_index";

        // First, open the directory
        Directory dir = null;

        // Then, open an IndexReader to access your index
        IndexReader indexReader = null;
        try {
            dir = FSDirectory.open(new File(pathIndex).toPath());
            indexReader = DirectoryReader.open(dir);
            String query = "query reformulation";
            List<String> queryTerms = LuceneUtils.tokenize(query, analyzer);

            long sum=0,doclen=0;
            List<Pair<String,Long>> largestDoc= new ArrayList<>();
            largestDoc.add(new Pair<>("zzz",-999L));

            //Compute Average Length
            for(int i=0;i<indexReader.numDocs();i++)
            {
                if(indexReader.getTermVector( i, "text" )!=null) {
                    TermsEnum termsEnum = indexReader.getTermVector(i, "text").iterator();
                    while (termsEnum.next() != null) {
                        doclen += termsEnum.totalTermFreq();
                    }

                    //Find biggest doc
                    if (largestDoc.get(0).getValue() < doclen) {
                        largestDoc = new ArrayList<>();
                        largestDoc.add(new Pair<>(indexReader.document(i).getField("docno").stringValue(), doclen));
                    } else if (largestDoc.get(0).getValue() == doclen)
                        largestDoc.add(new Pair<>(indexReader.document(i).getField("docno").stringValue(), doclen));

                    sum += doclen;
                    doclen = 0;
                }
            }

            //Term Size
            int uniqueTerms=0;
            Fields fields = MultiFields.getFields(indexReader);
            Terms terms = fields.terms("text");
            TermsEnum iterator = terms.iterator();
            while(iterator.next() != null)
                uniqueTerms++;


            //Frequencies
            long informationDocFreq=indexReader.docFreq(new Term("text","information"));
            long retrievalDocFreq=indexReader.docFreq(new Term("text","retrieval"));

            double idfInformation = Math.log((indexReader.numDocs()+1)/(informationDocFreq+1));
            double idfRetrieval=Math.log((indexReader.numDocs()+1)/(retrievalDocFreq+1));

            //Compute Unique Words
            System.out.print("\n 1." + indexReader.numDocs());
            System.out.print("\n 2. Average length "+(double)sum/indexReader.numDocs());
            System.out.print("\n 3. Unique words "+uniqueTerms);
            System.out.print("\n 4. Largest Doc");
            for (Pair<String,Long> p:largestDoc)
                System.out.print("\n \t DocNo: "+p.getKey()+" Length: "+p.getValue());
            System.out.print("\n 5. Words counts of information and retrieval");
            System.out.print("\n\tinformation count:"+informationDocFreq+"\tidf:"+idfInformation);
            System.out.print("\n\tretrieval count:"+retrievalDocFreq+"\tidf:"+idfRetrieval);

            System.out.print("\n 6. No of occurences of Query and Reformulation togther ");
            try {
                List<String> results=searchBooleanAND(indexReader, "text","query","reformulation");
                System.out.print("\n Total length "+results.size());
                for (String result:results)
                    System.out.print("\n Doc Id :"+result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            indexReader.close();
            dir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.print("\n Total Program Time " + String.valueOf(System.currentTimeMillis() - startime));
        }

    }

    public static List<String> searchBooleanAND(IndexReader index, String field, String q1,String q2) throws Exception {

        List<String> queryList=new ArrayList<>();
        List<String> reformulationList=new ArrayList<>();

        IndexSearcher searcher=new IndexSearcher(index);

        //Get all Doc Ids for query
        TopDocs docs = searcher.search(new TermQuery(new Term(field, q1)), index.numDocs());
        for(int i=0;i<docs.totalHits;i++) {
            queryList.add(LuceneUtils.getDocno(index, "docno", docs.scoreDocs[i].doc));
        }

        //Get all Doc Ids for reformulation
        docs = searcher.search(new TermQuery(new Term(field, q2)), index.numDocs());
        for(int i=0;i<docs.totalHits;i++) {
            reformulationList.add(LuceneUtils.getDocno(index, "docno", docs.scoreDocs[i].doc));
        }


        //Manual merge to retain the common items of the smallest list.
        if(reformulationList.size()>queryList.size()){
            reformulationList.retainAll(queryList);
            Collections.sort(reformulationList, String::compareTo);
            return reformulationList;
        }
        else {
            queryList.retainAll(reformulationList);
            Collections.sort(queryList, String::compareTo);
            return queryList;
        }

    }
}
