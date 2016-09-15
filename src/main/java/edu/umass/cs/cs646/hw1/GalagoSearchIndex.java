package edu.umass.cs.cs646.hw1;

import edu.umass.cs.cs646.utils.SearchResult;
import org.lemurproject.galago.core.index.IndexPartReader;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GalagoSearchIndex {
	
	public static void main( String[] args ) {
		try {
			
			// your own index path
			String pathIndex = "";
			// the folder to output your search results
			String pathOutput = "";
			
			// the query and the field to search the query
			String field = "text";
			String query = "query reformulation";
			
			// tokenize the query terms
			List<String> queryTerms = new ArrayList<>();
			Collections.addAll( queryTerms, query.toLowerCase().split( "[^a-zA-Z0-9]+" ) );
			
			File pathPosting = new File( new File( pathIndex ), "field." + field );
			
			DiskIndex index = new DiskIndex( pathIndex );
			IndexPartReader posting = DiskIndex.openIndexPart( pathPosting.getAbsolutePath() );
			Retrieval retrieval = RetrievalFactory.instance( pathIndex );
			
			List<SearchResult> resultsBooleanAND = searchBooleanAND( index, posting, retrieval, field, queryTerms );
			List<SearchResult> resultsTFIDF = searchTFIDF( index, posting, retrieval, field, queryTerms );
			List<SearchResult> resultsVSMCosine = searchVSMCosine( index, posting, retrieval, field, queryTerms );
			
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
			
			retrieval.close();
			posting.close();
			index.close();
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Perform a Boolean AND search and return the search results.
	 *
	 * @param index      A Galago DiskIndex object.
	 * @param posting    An IndexPartReader for the posting list.
	 * @param retrieval  A Galago Retrieval object.
	 * @param field      The field to search the query.
	 * @param queryTerms A list of tokenized query terms.
	 * @return A ranked list of search results.
	 * @throws IOException
	 */
	public static List<SearchResult> searchBooleanAND( DiskIndex index, IndexPartReader posting, Retrieval retrieval, String field, List<String> queryTerms ) throws IOException {
		// Write you implementation Problem 2 "Boolean AND" here
		return null;
	}
	
	/**
	 * Perform a Boolean AND search and return the search results.
	 *
	 * @param index      A Galago DiskIndex object.
	 * @param posting    An IndexPartReader for the posting list.
	 * @param retrieval  A Galago Retrieval object.
	 * @param field      The field to search the query.
	 * @param queryTerms A list of tokenized query terms.
	 * @return A ranked list of search results.
	 * @throws IOException
	 */
	public static List<SearchResult> searchTFIDF( DiskIndex index, IndexPartReader posting, Retrieval retrieval, String field, List<String> queryTerms ) throws Exception {
		// Write you implementation Problem 2 "Boolean OR" here
		return null;
	}
	
	/**
	 * Perform a Boolean AND search and return the search results.
	 *
	 * @param index      A Galago DiskIndex object.
	 * @param posting    An IndexPartReader for the posting list.
	 * @param retrieval  A Galago Retrieval object.
	 * @param field      The field to search the query.
	 * @param queryTerms A list of tokenized query terms.
	 * @return A ranked list of search results.
	 * @throws IOException
	 */
	public static List<SearchResult> searchVSMCosine( DiskIndex index, IndexPartReader posting, Retrieval retrieval, String field, List<String> queryTerms ) throws Exception {
		// Write you implementation Problem 2 "VSM (cosine similarity)" here
		return null;
	}
	
}
