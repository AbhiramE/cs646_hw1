package edu.umass.cs.cs646.hw1;

import edu.umass.cs.cs646.utils.LuceneUtils;
import edu.umass.cs.cs646.utils.SearchResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.function.docvalues.DocTermsIndexDocValues;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class LuceneSearchIndex {

    static // Analyzer includes options for text processing
            Analyzer analyzer = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
            TokenStreamComponents ts = new TokenStreamComponents(new StandardTokenizer());
            // Step 2: transforming all tokens into lowercased ones
            ts = new TokenStreamComponents(ts.getTokenizer(), new LowerCaseFilter(ts.getTokenStream()));
            // Step 3: whether to remove stop words
            // Uncomment the following line to remove stop words
            // ts = new TokenStreamComponents( ts.getTokenizer(), new StopwordsFilter( ts.getTokenStream(), StandardAnalyzer.ENGLISH_STOP_WORDS_SET ) );
            // Step 4: whether to apply stemming
            // Uncomment the following line to apply Krovetz or Porter stemmer
            // ts = new TokenStreamComponents( ts.getTokenizer(), new KStemFilter( ts.getTokenStream() ) );
            // ts = new TokenStreamComponents( ts.getTokenizer(), new PorterStemFilter( ts.getTokenStream() ) );
            return ts;
        }
    };

    public static void main(String[] args) {
        try {

            // your own index path
            String pathIndex = "/home/abhiram/codebase/InformationRetrieval/acm_corpus_index";
            // the folder to output your search results
            String pathOutput = "/home/abhiram/codebase/InformationRetrieval/acm_corpus_output";

            // the query and the field to search the query
            String field = "text";
            String query = "query reformulation";


            // tokenize the query into words (make sure you are using the same analyzer you used for indexing)
            List<String> queryTerms = LuceneUtils.tokenize(query, analyzer);

            Directory dir = FSDirectory.open(new File(pathIndex).toPath());
            IndexReader index = DirectoryReader.open(dir);

            //List<SearchResult> resultsBooleanAND = searchBooleanAND(index, field, queryTerms);
            //List<SearchResult> resultsTFIDF = searchTFIDF(index, field, queryTerms);
            List<SearchResult> resultsVSMCosine = searchVSMCosine(index, field, queryTerms);

            // do not change the following outputs
            File dirOutput = new File(pathOutput);
            dirOutput.mkdirs();

            /*
            {
                PrintStream writer = new PrintStream(new FileOutputStream(new File(dirOutput, "results_BooleanAND")));
                SearchResult.writeTRECFormat(writer, "0", "BooleanAND", resultsBooleanAND, resultsBooleanAND.size());
                SearchResult.writeTRECFormat(System.out, "0", "BooleanAND", resultsBooleanAND, 10);
                writer.close();
            }


            {
                PrintStream writer = new PrintStream(new FileOutputStream(new File(dirOutput, "results_TFIDF")));
                SearchResult.writeTRECFormat(writer, "0", "TFIDF", resultsTFIDF, resultsTFIDF.size());
                SearchResult.writeTRECFormat(System.out, "0", "TFIDF", resultsTFIDF, 10);
                writer.close();
            }

            */
            {
                PrintStream writer = new PrintStream(new FileOutputStream(new File(dirOutput, "results_VSMCosine")));
                SearchResult.writeTRECFormat(writer, "0", "VSMCosine", resultsVSMCosine, resultsVSMCosine.size());
                SearchResult.writeTRECFormat(System.out, "0", "VSMCosine", resultsVSMCosine, 10);
                writer.close();
            }

            index.close();
            dir.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform a Boolean AND search and return the search results.
     *
     * @param index      A Lucene index reader.
     * @param field      The index field to search the query.
     * @param queryTerms A list of tokenized query terms.
     * @return A list of search results (sorted by relevance scores).
     */
    public static List<SearchResult> searchBooleanAND(IndexReader index, String field, List<String> queryTerms) throws Exception {

        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        List<SearchResult> results=new ArrayList<>();

        IndexSearcher searcher=new IndexSearcher(index);
        List<Term> terms= queryTerms.stream().map(queryTerm -> new Term(field, queryTerm)).collect(Collectors.toList());
        List<Query> queries= terms.stream().map(TermQuery::new).collect(Collectors.toList());

        for (Query query:queries)
            booleanQuery.add(query,BooleanClause.Occur.MUST);

        //Get all hits
        TopDocs topDocs = searcher.search(booleanQuery.build(),index.numDocs());

        for(int i=0;i<topDocs.totalHits;i++) {
            results.add(new SearchResult(topDocs.scoreDocs[i].doc,
                    LuceneUtils.getDocno(index, "docno", topDocs.scoreDocs[i].doc),
                    topDocs.scoreDocs[i].score));
        }
        System.out.print("Total length "+results.size());

        Collections.sort(results, (a, b) -> a.getDocno().compareTo(b.getDocno()));

        for (SearchResult result:results)
            System.out.print("\n Doc Id :"+result.getDocno());

        System.out.print("\n\n");

        return results;
    }

    /**
     * Perform a TFxIDF search and return the search results.
     *
     * @param index      A Lucene index reader.
     * @param field      The index field to search the query.
     * @param queryTerms A list of tokenized query terms.
     * @return A list of search results (sorted by relevance scores).
     */
    public static List<SearchResult> searchTFIDF(IndexReader index, String field, List<String> queryTerms) throws IOException {
        // Write you implementation Problem 2 "TFxIDF" here
        List<Double> scores = new ArrayList<>();


        for (int i=0;i<index.maxDoc();i++){
                double frequency=0,idfTf=0; BytesRef term;
            for (String queryTerm : queryTerms) {
                double docFrequency = index.docFreq(new Term("text", queryTerm));
                double idf = Math.log((index.numDocs() + 1) / (docFrequency + 1));

                TermsEnum iterator=index.getTermVector(i,field).iterator();
                while ((term=iterator.next())!=null) {
                    if(term.utf8ToString().equals(queryTerm)) {
                        frequency = iterator.totalTermFreq(); //Find term frequency in that doc
                        break;
                    }
                }
                idfTf+=idf*frequency;           //Add to IDF TF product for that query term
            }
            scores.add(idfTf);
        }

        List<SearchResult> results=new ArrayList<>();
        for (int i=0;i<index.maxDoc();i++)
        {
            results.add(new SearchResult(i,
                    LuceneUtils.getDocno(index, "docno", i),
                    scores.get(i)));
        }

        //Sort
        Collections.sort(results, (a, b) -> b.getScore().compareTo(a.getScore()));

        for(int i=0;i<20;i++)
        {
            System.out.print("\n "+results.get(i).getDocid()+" "+
                    results.get(i).getDocno()+" "+results.get(i).getScore());
        }
        System.out.println();

        return results;
    }

    /**
     * Perform a VSM (cosine similarity) search and return the search results.
     *
     * @param index      A Lucene index reader.
     * @param field      The index field to search the query.
     * @param queryTerms A list of tokenized query terms.
     * @return A list of search results (sorted by relevance scores).
     */
    public static List<SearchResult> searchVSMCosine(IndexReader index, String field, List<String> queryTerms) throws IOException {

        List<String> uniqueQueryTerms=queryTerms.stream().distinct().collect(Collectors.toList());
        List<Integer> queryTermFrequencies=new ArrayList<>();
        List<List<Double>> documentTermFrequencies =new ArrayList<>();
        double sumOfDocumentTermFrequenciesSquare=0;

        //Compute Query Frequencies if there are duplicate terms in query
        if(uniqueQueryTerms.size()<queryTerms.size())
        {
            for(int i=0;i<uniqueQueryTerms.size();i++) {
                String uniqueTerm = uniqueQueryTerms.get(i);
                int count=0;
                for (String queryTerm : queryTerms) {
                    if (uniqueTerm.equals(queryTerm))
                        count++;
                }
                queryTermFrequencies.add(i,count);
            }
        }else
            queryTermFrequencies.addAll(uniqueQueryTerms.stream().map(uniqueTerm -> 1).collect(Collectors.toList()));


        //Find total term frequency of each query term in a doc
        for (int i=0;i<index.maxDoc();i++) {
            double frequency=0;
            BytesRef term;
            List<Double> frequencies=new ArrayList<>();
            for (String queryTerm : queryTerms) {
                TermsEnum iterator = index.getTermVector(i, field).iterator();
                while ((term = iterator.next()) != null) {
                    if (term.utf8ToString().equals(queryTerm)) {
                        frequency = iterator.totalTermFreq(); //Find term frequency in that doc
                        break;
                    }
                }
                sumOfDocumentTermFrequenciesSquare+=Math.pow(frequency,2);
                frequencies.add(frequency);
            }
            documentTermFrequencies.add(frequencies);
        }

        //Find the sum of query frequencies
        double sumOfQueryTermFrequencies=0;
        for(int queryTf:queryTermFrequencies)
            sumOfQueryTermFrequencies+=Math.pow(queryTf,2);

        List<Double> scores=new ArrayList<>();

        //Computer Cosine Similarity for all docs
        for(int i=0;i<index.maxDoc();i++)
        {
            double product=0;
            for (int j=0; j<uniqueQueryTerms.size();j++) {
                product=queryTermFrequencies.get(j)*documentTermFrequencies.get(i).get(j);
            }
            scores.add(product/
                    (Math.sqrt(sumOfDocumentTermFrequenciesSquare)*Math.sqrt(sumOfQueryTermFrequencies)));
        }

        List<SearchResult> results=new ArrayList<>();
        for (int i=0;i<index.maxDoc();i++)
        {
            results.add(new SearchResult(i,
                    LuceneUtils.getDocno(index, "docno", i),
                    scores.get(i)));
        }

        //Sort
        Collections.sort(results, (a, b) -> b.getScore().compareTo(a.getScore()));

        for(int i=0;i<10;i++)
        {
            System.out.print("\n "+results.get(i).getDocid()+" "+
                    results.get(i).getDocno()+" "+results.get(i).getScore());
        }
        System.out.println();

        return results;
    }

}
