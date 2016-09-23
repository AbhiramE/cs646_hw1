package edu.umass.cs.cs646.hw1;

import edu.umass.cs.cs646.utils.SearchResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Evaluation {
	
	public static void main( String[] args ) {
		try {
			
			// path of the search results folder
			String pathResults = "/home/abhiram/codebase/InformationRetrieval/acm_corpus_output";
			// path of the qrels file
			String pathQrels = "/home/abhiram/codebase/InformationRetrieval/cs646_hw1/qrels";
			
			Set<String> relDocnos = new TreeSet<>();
			BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( pathQrels ), "UTF-8" ) );
			String line;
			while ( ( line = reader.readLine() ) != null ) {
				String[] splits = line.split( "\\s+" );
				if ( splits[1].equals( "1" ) ) {
					relDocnos.add( splits[0] );
				}
			}
			reader.close();
			
			System.out.printf( "%-15s%-6s%-6s%-6s%-6s%-6s%-6s%-6s\n", "MODEL", "P@5", "P@10", "P@20", "R@10", "R@20", "R@100", "AP" );
			for ( String model : new String[]{ "BooleanAND", "TFIDF", "VSMCosine" } ) {
				List<SearchResult> results = SearchResult.readTRECFormat( new File( new File( pathResults ), "results_" + model ) ).get( "0" );
				double p5 = evalPrecision( results, relDocnos, 5 ); // Precision at rank 5
				double p10 = evalPrecision( results, relDocnos, 10 ); // Precision at rank 10
				double p20 = evalPrecision( results, relDocnos, 20 ); // Precision at rank 20
				double r10 = evalRecall( results, relDocnos, 10 ); // Recall at rank 10
				double r20 = evalRecall( results, relDocnos, 20 ); // Recall at rank 20
				double r100 = evalRecall( results, relDocnos, 100 ); // Recall at rank 100
				double ap = evalAP( results, relDocnos );
				System.out.printf( "%-15s%-6.2f%-6.2f%-6.2f%-6.2f%-6.2f%-6.2f%-6.2f\n", model, p5, p10, p20, r10, r20, r100, ap );
				//System.out.printf( "%-15s%-6.2f%-6.2f%-6.2f%-6.2f%-6.2f%-6.2f\n", model, p5, p10, p20,r10,r20,r100);
			}
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Compute precision at rank k.
	 *
	 * @param results   A ranked list of search results.
	 * @param relDocnos The set of relevant documents.
	 * @param k         The cutoff rank k.
	 * @return
	 */
	public static double evalPrecision( List<SearchResult> results, Set<String> relDocnos, int k ) {
		// write your implementation for problem 3 "P@k" here
        int rk=0;
        for (int i=0;i<k;i++)
            if(relDocnos.contains(results.get(i).getDocno()))
                rk++;

		return (double) rk/k;
	}
	
	/**
	 * Compute recall at rank k.
	 *
	 * @param results   A ranked list of search results.
	 * @param relDocnos The set of relevant documents.
	 * @param k         The cutoff rank k.
	 * @return
	 */
	public static double evalRecall( List<SearchResult> results, Set<String> relDocnos, int k ) {

        int rk=0;
        for (int i=0;i<k;i++)
            if(relDocnos.contains(results.get(i).getDocno()))
                rk++;

        return (double) rk/relDocnos.size();
	}
	
	/**
	 * Compute the average precision of the whole ranked list.
	 *
	 * @param results   A ranked list of search results.
	 * @param relDocnos The set of relevant documents.
	 * @return
	 */
	public static double evalAP( List<SearchResult> results, Set<String> relDocnos ) {

        double totalPrecision=0;
        for (int i=0;i<results.size();i++)
            if(relDocnos.contains(results.get(i).getDocno()))
                totalPrecision=evalPrecision(results,relDocnos,i);

		return totalPrecision/relDocnos.size();
	}
	
}
