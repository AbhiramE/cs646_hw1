package edu.umass.cs.cs646.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.util.BytesRef;
import org.lemurproject.galago.core.parse.stem.Porter2Stemmer;
import org.lemurproject.galago.krovetz.KStem;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Some Lucene utilities for CS646 students.
 * Feel free to use them in your homework.
 */
public class LuceneUtils {
	
	/**
	 * Find a document in the index by its docno (external ID).
	 * Returns the internal ID of the document; or -1 if not found.
	 *
	 * @param index      An index reader.
	 * @param fieldDocno The name of the field you used for storing docnos (external document IDs).
	 * @param docno      The docno (external ID) you are looking for.
	 * @return The internal ID of the document in the index; or -1 if not found.
	 * @throws IOException
	 */
	public static int findByDocno( IndexReader index, String fieldDocno, String docno ) throws IOException {
		BytesRef term = new BytesRef( docno );
		PostingsEnum posting = MultiFields.getTermDocsEnum( index, fieldDocno, term, PostingsEnum.NONE );
		if ( posting != null ) {
			int docid = posting.nextDoc();
			if ( docid != PostingsEnum.NO_MORE_DOCS ) {
				return docid;
			}
		}
		return -1;
	}
	
	/**
	 * @param index      An index reader.
	 * @param fieldDocno The name of the field you used for storing docnos (external document IDs).
	 * @param docid      The internal ID of the document
	 * @return The docno (external ID) of the document.
	 * @throws IOException
	 */
	public static String getDocno( IndexReader index, String fieldDocno, int docid ) throws IOException {
		// This implementation is just for you to quickly understand how this works.
		// You should consider reuse the fieldset if you need to read docnos for a lot of documents.
		Set<String> fieldset = new HashSet<>();
		fieldset.add( fieldDocno );
		Document d = index.document( docid, fieldset );
		return d.get( fieldDocno );
	}
	
	/**
	 * Tokenize the input text into tokens/words using a Lucene analyzer.
	 *
	 * @param text     An input text.
	 * @param analyzer An Analyzer.
	 * @return A list of tokenized words.
	 */
	public static List<String> tokenize( String text, Analyzer analyzer ) throws IOException {
		List<String> tokens = new ArrayList<>();
		TokenStream ts = analyzer.tokenStream( "", new StringReader( text ) );
		CharTermAttribute attr = ts.getAttribute( CharTermAttribute.class );
		ts.reset();
		while ( ts.incrementToken() ) {
			tokens.add( attr.toString() );
		}
		ts.end();
		ts.close();
		return tokens;
	}
	
	private static Porter2Stemmer porter2 = new Porter2Stemmer();
	private static KStem krovetz = new KStem();
	
	public static String stemPorter2( String word ) {
		return porter2.stem( word );
	}
	
	public static String stemKrovetz( String word ) {
		return krovetz.stemTerm( word );
	}
	
}
