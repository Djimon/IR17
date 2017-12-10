
/*
 * HINWEIS/ INFO: laut Aufgabenstellung soll das Programm folgendermaßen
 * aufgerufen werden: java -jar IR_P01.jar [pathtodocumentfolder] [pathtoindexfolder] [VS/OK] [query] 
 * Ausführung mit diesen parametern funktioniert bereits, sogar mit beliebig langer query 
 * (IR_P01.jar im Ordner "release")
 *
 * Aufgabenstellung:  
  	Lucene is an open source search library that provides
	standard functionality for analyzing, indexing, and searching text-based documents.  The
	following criteria have to be met by your Information Retrieval system.
	
	[X] Using  Lucene,  parse  and  index HTML documents  that  a  given  folder  and  its subfolders 
		contain.  List all parsed files!!!!
	[X] Consider the English language and use a stemmer for it (e.g. Porter Stemmer)
	[X] Select an available search index or create a new one (if not available in the chosen directory)
	[X] Make possible for the user to choose the ranking model, Vector Space Model (VS) 
		or Okapi BM25 (OK) -> beinhaltet auch das Berechnen der einzelnen rankings
	[X] Print  a  ranked  list  of  relevant  articles  given  a  search  query.   The  output  should
		contain 10 most relevant documents with their rank, title and summary, relevance score and path.
	[X?] Search   multiple   fields   concurrently   (multifield   search): not   only   search   the
		document’s text (body tag), but also its title
	
	[X] Create  a  jar-File  named  IR_P01.jar. 
	[X] It should process the input: 
		> java -jar IRP01.jar [pathtodocumentfolder] [pathtoindexfolder] [VS/OK] [query]		

 */

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;


enum rankingModel 
{
	VectorSpace, OkapiBM25, invalid
}

enum Fieldz 
{
	title, content, path, summary
}

public class LuceneTest 
{
	// ***************************************************** \\
	// ****************** MAIN METHODE ********************* \\
	// ***************************************************** \\

	public static void main(String[] args) 
	{
		String docFolder = null;
		String indexFolder = null;
		rankingModel ranking = rankingModel.invalid;
		ArrayList<String> query = new ArrayList<String>();
		LuceneTest SearchObject = null;

		if (args.length >= 3) {
			docFolder = args[0];
			indexFolder = args[1];
			if (!new File(indexFolder).isDirectory())
				ErrorAndExit("Argument 2 is no directory:" + indexFolder);
			ranking = (args[2].equals("VS") ? rankingModel.VectorSpace
					: args[2].equals("OK") ? rankingModel.OkapiBM25
							: rankingModel.invalid);
		} else {
			ErrorAndExit("arguments missing - programm needs at least 4");
		}

		if (ranking == rankingModel.invalid) {
			ErrorAndExit("entered invalid ranking model(" + args[2] + ")");
		} else {
			for (int i = 3; i < args.length; i++) {
				query.add(args[i]);
				// System.out.println("arg "+i +": " + args[i]);
			}

			SearchObject = new LuceneTest(docFolder, indexFolder, ranking,
					query);

			System.out.println("Successfully instantiated SearchObject:");
			//System.out.println(SearchObject.toString());

			// **********************************
			// TODO: COMPLETE SEARCH LOGIC HERE!!
			// **********************************
			SearchObject.SetSimilarityMethod();

			SearchObject.SelectIndex();
			SearchObject.CreateIndexReaderAndSearcher();
			
			SearchObject.Search(10);
			SearchObject.PrintResults();
		}
	}
	
	// ***************************************************** \\
	// ****************** Class Attributes ***************** \\
	// ***************************************************** \\
	
	ArrayList<SearchResult> SearchOutput = new ArrayList<SearchResult>();
	EnglishAnalyzer AnalyzerIncludingStemmer = new EnglishAnalyzer();
	private IndexReader IR;
	private IndexSearcher ISearch;
	private FSDirectory indexFSD = null;
	Similarity SIM = null;
	private String docPath;
	private String indexPath;
	// Friedrich added the following:
	private static final String fieldPath = "path";
	public static final String fieldContents = "contents";

	private rankingModel ranking;
	private String querryArray[];
	private String querryString = "";
	private ArrayList<Document> docList = new ArrayList<Document>();
	private ArrayList<Document> stemmedDocList = new ArrayList<Document>();
	private IndexWriter writer;
	private File indexDirectory;
	private Path path;
	private Query Qquery;
	
	// CONSTRUCTOR
	public LuceneTest (String docs, String index, rankingModel rank, ArrayList<String> query) 
	{
		this.querryArray = new String[query.size()];
		for (int i = 0; i < query.size(); i++) {
			this.querryArray[i] = query.get(i);
			this.querryString += query.get(i) + " ";
			// System.out.println("get "+query.get(i));
		}
		this.docPath = docs;
		this.indexPath = index;
		this.ranking = rank; 
		this.path = FileSystems.getDefault().getPath(indexPath);
		//TODO: make query object
			
	}
	
	/*
 	 Metafunction calling the consecutive functions to get all source files and processing it with the IndexWriter. 	
	 */
	private void SelectIndex() 
	{	
		createIndexWriter();
		ParseDocsinGivenFolder();
		//validateFiles();
		closeIndexWriter();
	}
	
	/*
	 Creates a new indexwriter after checking if directory exists
	 */
	private void createIndexWriter()
	{
		
		try{
			indexDirectory = new File(indexPath);
			if(!indexDirectory.exists())
			{
				indexDirectory.mkdir();
			}
			indexFSD = FSDirectory.open(new File(this.indexPath).toPath());
			
			IndexWriterConfig config = new IndexWriterConfig(this.AnalyzerIncludingStemmer);
			config.setSimilarity(this.SIM);
			this.writer = new IndexWriter(indexFSD, config);
		}
		catch(Exception e){
			System.out.println("Couldn't get the Index Writer.");
			e.printStackTrace();
		}
		
	}
	
	/*
	 Gets all files at given source file path and checks if it can be used. If usable it will be processed and indexed.
	 */
	private void validateFiles()
	{
		File[] Sourcefiles = new File[new File(docPath).list().length];
		Sourcefiles = new File(docPath).listFiles();
		
		for(File f : Sourcefiles)
		{
			try{
				// Checks if file can be used
				if(!f.isDirectory() && f.exists() && f.isFile() && !f.isHidden() 
					&& f.canRead() && f.length() > 0 && f.getName().endsWith(".html"))
				{
					System.out.println("Currently trying to index file: " + f.getAbsolutePath());
					// calling function to actually index the file
					indexFile(f);
					System.out.println("Successfully index file: " + f.getAbsolutePath());
				}
			}
			catch(Exception e)
			{
				System.out.println("Couldn't index current file at " + f.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}
	
	/*
	 Converting all files into multifield documents with getDocument(), then adding it to the
	 Index Writer and therefor indexing the file.
	 */
	private void indexFile(File f)
	{
		try {
			Document d = getDocument(f, getJsoupStrings(f));
			if(d != null)
			{
				writer.addDocument(d);
			}
		} catch (IOException e) {
			System.out.println("Couldn't get Document.");
			e.printStackTrace();
		}	
	}
	
	/*
	 Closing the Index Writer after processing all files.
	 */
	private void closeIndexWriter()
	{
		try{
			writer.close();
		}
		catch(Exception e)
		{
			System.out.println("Couldn't close Index Writer.");
			e.printStackTrace();
		}
	}
	
	private Document getDocument(File file, jsoupResultStrings result) throws IOException {
		// TASK: Method to get a lucene document from HTML files in folder
		// create various types of fields
		Document document = new Document();
		
		// index file contents
		Field contentField = new TextField(Fieldz.title.name(), result.getBody(), Store.YES);
		// index file name
		Field fileNameField = new TextField(Fieldz.content.name(), result.getTitle(), Store.YES);
		// index file path
		Field filePathField = new TextField(Fieldz.path.name(),file.getCanonicalPath(), Store.YES);
		// index file summary
		Field summaryField = new TextField(Fieldz.summary.name(), result.getSummary(), Store.YES);

		document.add(contentField);
		document.add(fileNameField);
		document.add(filePathField);
		document.add(summaryField);

		return document;
	}
	
	private jsoupResultStrings getJsoupStrings(File file)
	{	
		// create documents via JSOUP(jsoup.org) 
		org.jsoup.nodes.Document doc = Jsoup.parse(docPath); 
		
		Element summ = doc.select("summary").first();
		String s = (summ == null ? "":summ.html());
		if (s.length() <= 0)
			s  = doc.body().text().substring(0, Math.min(doc.body().text().length(), 17)) + "...";
		 //only one document import (jsoup collides with lucene) 
		 //ouput the title and the body content 
		 jsoupResultStrings result = new jsoupResultStrings(doc.title(),doc.body().text(), s);
		 return result;
	}

	private void ParseDocsinGivenFolder() 
	{
		System.out.println("parsing the folder: "+ this.docPath);
		File folder = new File(this.docPath);
		//System.out.println("foldername: " + folder.toString());
		ArrayList<File> filesInFolder = new ArrayList<File>();
		try 
		{
			filesInFolder = (ArrayList<File>) Files.walk(Paths.get(docPath))
											       .filter(Files::isRegularFile)
											       .map(Path::toFile)
											       .collect(Collectors.toList());
		} 
		catch (IOException e) {System.out.println("Error: " +e);}
		
		showFiles(filesInFolder); 					
	}

	private void showFiles(ArrayList<File> files) {
		// TODO: Parse the whole folder (including subfolders) and list the html-documents
		for (File file : files) {
			if (file.isDirectory()) 
			{
				//System.out.println("Directory: " + file.getName());
				ArrayList<File> F = new ArrayList<File>();
				try 
				{
					F = (ArrayList<File>) Files.walk(Paths.get(file.getCanonicalPath()))
						       .filter(Files::isRegularFile)
						       .map(Path::toFile)
						       .collect(Collectors.toList());
				} 
				catch (IOException e) {System.out.println("Error: "+e);}
				showFiles(F);  //recursion
			} 
			else 
			{
				//System.out.println("File: " + file.getName());				
				try 
				{
					Document D = getDocument(file, getJsoupStrings(file));
					if (D != null)
						writer.addDocument(D);
				} 
				catch (IOException e) {ErrorAndExit(e.toString());}
			}
		}
	}

	private void CreateIndexReaderAndSearcher() 
	{
		if (this.indexFSD == null)
			ErrorAndExit("couldn't find indexDirectory");
		else
		{
			try 
			{
				this.IR = DirectoryReader.open(this.indexFSD); 
			} 
			catch (IOException e) {System.out.println("Error while instantiating index reader." + e);}
					
			this.ISearch = new IndexSearcher(this.IR);
			ISearch.setSimilarity(SIM);
		}

	}

	private void SetSimilarityMethod() 
	{
		if(this.ranking.equals(rankingModel.OkapiBM25))
		{
			SIM = new BM25Similarity();			
		}
		else if(this.ranking.equals(rankingModel.VectorSpace))
		{
			SIM = new TFIDFSimilarity() {
		        @Override
		        public float tf(float freq) {
		          return (float)Math.sqrt(freq);
		        }
		        @Override
		        public float sloppyFreq(int distance) {
		          return 1.0f / (distance + 1);
		        }
		        @Override
		        public float scorePayload(int arg0, int arg1, int arg2, BytesRef arg3) {
		          return 1;
		        }
		        @Override
		        public float lengthNorm(int numTerms) {
		          return (float) (1/Math.sqrt(numTerms));
		        }
		        @Override
		        public float idf(long docFreq, long numDocs) {
		          return (float)(Math.log(numDocs/(double)(docFreq+1)) + 1.0);
		        }
		      };		      
		}
	}
	
	private void Search(int bestN) 
	{
		/* TODO: Do the actual searching with:
		 * - calculating tf-idf
		 * - relevance score
		 * - ranking
		 * - konkurierende Suche in Titel und Textkörper (mulrifield search)
		 */		
		ScoreDoc[] hits = null;
		Query Qtitle = null;
		Query Qbody = null;
		try 
		{
			Qtitle = new QueryParser(Fieldz.title.name(), this.AnalyzerIncludingStemmer).parse(this.querryString);
			Qbody = new QueryParser(Fieldz.content.name(), this.AnalyzerIncludingStemmer).parse(this.querryString);
			System.out.println("Searching for \"" + this.querryString + "\"...");
		} 
		catch (ParseException e1) {ErrorAndExit(e1.toString());}
		
		try 
		{
			System.out.println("Ranking the best 10 documents...");
			TopDocs docsTitle = ISearch.search(Qtitle, bestN);
			System.out.println("hits in title = " + docsTitle.totalHits);
			TopDocs docsBody = ISearch.search(Qbody, bestN);
			System.out.println("hits in body = " + docsTitle.totalHits);
	        hits = TopDocs.merge(bestN, new TopDocs[]{docsTitle,docsBody}).scoreDocs;       
		} 
		catch (IOException e2) {ErrorAndExit(e2.toString());}
		
		/* ERROR DESCRIPTION
		 * =================================================================================================
		 * Unfortunately our TopDocs array seems to be empty, so something is wrong with the search function
		 * We instantiated the IndexSearcher with the correct similarity method.
		 * During Debugging we checked the queries which seems to be O.K.
		 * So from here we did not really know, what the problem could be.
		 * =================================================================================================
		 */
		
		if (hits.length > 0)
		{
			for ( int i= 0; i< hits.length; i++)
			{
				int docID = hits[i].doc;
				Document D = null;
				
				try 
				{
					D = ISearch.doc(docID);
				} 
				catch (IOException e) {System.out.println("Error in Doc " + i);}
				
				if (D != null)
				{
					SearchOutput.add(new SearchResult(i+1, 
													  D.get(Fieldz.title.name()),
													  D.get(Fieldz.summary.name()),
													  hits[i].score,
													  D.get(Fieldz.path.name())));
				}		 
			}
		}
		else if (hits == null )
		{
			System.out.println("this shouldn't happen: The document score container is null.");
		}			
		else
			System.out.println("No hits were found.");		
	}

	private void PrintResults() 
	{
		for (int i=0; i  < SearchOutput.size(); i++)
		{
			String w = SearchOutput.get(i).toString();
			System.out.println(w);
			//TODO: save results into file (nice 2 have)
		}	
	}

	public String toString() {
		String temp = "";
		for (String s : this.querryArray) {
			temp += s;
			temp += ",";
		}
		return (this.docPath + "\n" + this.indexPath + "\n"
				+ this.ranking.toString() + "\n" + temp);
	}

	private static void ErrorAndExit(String errorMsg) {
		System.out.println("ERROR:" + errorMsg);
		System.out.println("Press any key to close...");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		System.exit(0);
	}
}
