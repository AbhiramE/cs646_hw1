package edu.umass.cs.cs646.hw1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuceneBuildIndex {
	
	public static void main( String[] args ) {
		try {
			
			String pathCorpus = "/home/abhiram/codebase/InformationRetrieval/acm_corpus"; // path of the corpus file
			String pathIndex = "/home/abhiram/codebase/InformationRetrieval/acm_corpus_index"; // path of the index directory
			
			Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
			
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
			
			IndexWriterConfig config = new IndexWriterConfig( analyzer );
			// Note that IndexWriterConfig.OpenMode.CREATE will override the original index in the folder
			config.setOpenMode( IndexWriterConfig.OpenMode.CREATE );
			
			IndexWriter ixwriter = new IndexWriter( dir, config );
			
			// This is the field setting for metadata field.
			FieldType fieldTypeMetadata = new FieldType();
			fieldTypeMetadata.setOmitNorms( true );
			fieldTypeMetadata.setIndexOptions( IndexOptions.DOCS );
			fieldTypeMetadata.setStored( true );
			fieldTypeMetadata.setTokenized( false );
			fieldTypeMetadata.freeze();
			
			// This is the field setting for normal text field.
			FieldType fieldTypeText = new FieldType();
			fieldTypeText.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS );
			fieldTypeText.setStoreTermVectors( true );
			fieldTypeText.setStoreTermVectorPositions( true );
			fieldTypeText.setTokenized( true );
			fieldTypeText.setStored( true );
			fieldTypeText.freeze();
			
			// You need to iteratively read each document from the corpus file,
			// create a Document object for the parsed document, and add that
			// Document object by calling addDocument().
			
			// write your impelemntation here
			
			// remember to close both the index writer and the directory

			Pattern docNoPattern = Pattern.compile("<DOCNO>(.+?)</DOCNO>",
					Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL);

			try (BufferedReader br = new BufferedReader(new FileReader(pathCorpus))) {
				String line;
				int i=0;
				while ((line = br.readLine()) != null) {

					String docNo=null,text = null;

					line.trim();
					if(line.equals("<DOC>"))
					{
						//Read Doc No
						line=br.readLine();
						Matcher docNoMatcher = docNoPattern.matcher(line);
						while (docNoMatcher.find())
							docNo=docNoMatcher.group(1).trim();

						//Read Lines till you get text
						br.readLine();

						//Read Text
						while (!(line = br.readLine().trim()).equals("</TEXT>"))
							text += line;

						Document d = new Document();
						if(docNo!=null)
							d.add(new Field("docno",docNo,fieldTypeMetadata));
						d.add(new Field("text",text,fieldTypeText));
						ixwriter.addDocument( d );
						System.out.print(" For "+i);
						i++;
					}
				}
			}

			ixwriter.close();
			dir.close();
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
}
