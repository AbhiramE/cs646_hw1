package edu.umass.cs.cs646.hw1;

import edu.umass.cs.cs646.utils.LuceneUtils;
import edu.umass.cs.cs646.utils.SearchResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class LuceneSearchIndex {
	
	public static void main( String[] args ) {
		try {
			
			// your own index path
			String pathIndex = "";
			// the folder to output your search results
			String pathOutput = "";
			
			// the query and the field to search the query
			String field = "text";
			String query = "query reformulation";
			
			// Analyzer includes options for text processing
			Analyzer analyzer = new Analyzer() {
				@Override
				protected TokenStreamComponents createComponents( String fieldName ) {
					// Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
					TokenStreamComponents ts = new TokenStreamComponents( new StandardTokenizer() );
					// Step 2: transforming all tokens into lowercased ones
					ts = new TokenStreamComponents( ts.getTokenizer(), new LowerCaseFilter( ts.getTokenStream() ) );
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
			
			// tokenize the query into words (make sure you are using the same analyzer you used for indexing)
			List<String> queryTerms = LuceneUtils.tokenize( query, analyzer );
			
			Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
			IndexReader index = DirectoryReader.open( dir );
			
			List<SearchResult> resultsBooleanAND = searchBooleanAND( index, field, queryTerms );
			List<SearchResult> resultsTFIDF = searchTFIDF( index, field, queryTerms );
			List<SearchResult> resultsVSMCosine = searchVSMCosine( index, field, queryTerms );
			
			// do not change the following outputs
			File dirOutput = new File( pathOutput );
			dirOutput.mkdirs();
			
			{
				PrintStream writer = new PrintStream( new FileOutputStream( new File( dirOutput, "results_BooleanAND" ) ) );
				SearchResult.writeTRECFormat( writer, "0", "BooleanAND", resultsBooleanAND, resultsBooleanAND.size() );
				SearchResult.writeTRECFormat( System.out, "0", "BooleanAND", resultsBooleanAND, 10 );
				writer.close();
			}
			
			{
				PrintStream writer = new PrintStream( new FileOutputStream( new File( dirOutput, "results_TFIDF" ) ) );
				SearchResult.writeTRECFormat( writer, "0", "TFIDF", resultsTFIDF, resultsTFIDF.size() );
				SearchResult.writeTRECFormat( System.out, "0", "TFIDF", resultsTFIDF, 10 );
				writer.close();
			}
			
			{
				PrintStream writer = new PrintStream( new FileOutputStream( new File( dirOutput, "results_VSMCosine" ) ) );
				SearchResult.writeTRECFormat( writer, "0", "VSMCosine", resultsVSMCosine, resultsVSMCosine.size() );
				SearchResult.writeTRECFormat( System.out, "0", "VSMCosine", resultsVSMCosine, 10 );
				writer.close();
			}
			
			index.close();
			dir.close();
			
		} catch ( Exception e ) {
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
	public static List<SearchResult> searchBooleanAND( IndexReader index, String field, List<String> queryTerms ) throws Exception {
		// Write you implementation Problem 2 "Boolean AND" here
		return null;
	}
	
	/**
	 * Perform a TFxIDF search and return the search results.
	 *
	 * @param index      A Lucene index reader.
	 * @param field      The index field to search the query.
	 * @param queryTerms A list of tokenized query terms.
	 * @return A list of search results (sorted by relevance scores).
	 */
	public static List<SearchResult> searchTFIDF( IndexReader index, String field, List<String> queryTerms ) throws IOException {
		// Write you implementation Problem 2 "TFxIDF" here
		return null;
	}
	
	/**
	 * Perform a VSM (cosine similarity) search and return the search results.
	 *
	 * @param index      A Lucene index reader.
	 * @param field      The index field to search the query.
	 * @param queryTerms A list of tokenized query terms.
	 * @return A list of search results (sorted by relevance scores).
	 */
	public static List<SearchResult> searchVSMCosine( IndexReader index, String field, List<String> queryTerms ) throws IOException {
		// Write you implementation Problem 2 "VSM (cosine similarity)" here
		return null;
	}
	
}
