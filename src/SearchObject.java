
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
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.tartarus.snowball.ext.PorterStemmer;


public class SearchObject 
{
	// ***************************************************** \\
	// ****************** Class Attributes ***************** \\
	// ***************************************************** \\
	
	ArrayList<SearchResult> SearchOutput = new ArrayList<SearchResult>();
	EnglishAnalyzer AnalyzerIncludingStemmer = new EnglishAnalyzer();
	PorterStemmer stemmer = new PorterStemmer();
	private IndexReader IR;
	private IndexSearcher ISearch;
	private FSDirectory indexFSD = null;
	Similarity SIM = null;
	private String[] URLArray;
	private String indexPath;
	// Friedrich added the following:
	private static final String fieldPath = "path";
	private static final String fieldContents = "contents";

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
	public SearchObject (String[] urlList, String index, rankingModel rank, ArrayList<String> query) 
	{
		this.querryArray = new String[query.size()];
		for (int i = 0; i < query.size(); i++) 
		{
			// Stemming the query and transform it into Array and String
			stemmer.setCurrent(query.get(i));
			stemmer.stem();
			this.querryArray[i] = stemmer.getCurrent();
			this.querryString += stemmer.getCurrent() + " ";
			// System.out.println("get "+query.get(i));
		}
		this.URLArray = urlList;
		this.indexPath = index;
		this.ranking = rank; 
		this.path = FileSystems.getDefault().getPath(indexPath);
		//TODO: make query object
			
	}
	
	/*
 	 Metafunction calling the consecutive functions to get all source files and processing it with the IndexWriter. 	
	 */
	public void SelectIndex() throws IOException 
	{	
		createIndexWriter();
		AddFilestoIndex(this.URLArray);
		//validateFiles();
		closeIndexWriter();
	}
	
	/*
	 Creates a new indexwriter after checking if directory exists
	 */
	public void createIndexWriter()
	{
		try{
			indexDirectory = new File(indexPath);
			if(!indexDirectory.exists())
			{
				System.out.println("new");
				indexDirectory.mkdir();
			}
			indexFSD = FSDirectory.open(new File(this.indexPath).toPath());
			
			IndexWriterConfig config = new IndexWriterConfig(this.AnalyzerIncludingStemmer);
			config.setSimilarity(this.SIM);
			// create a new index every time
			config.setOpenMode(OpenMode.CREATE);
			
			this.writer = new IndexWriter(indexFSD, config);
		}
		catch(Exception e){
			System.out.println("Couldn't get the Index Writer.");
			e.printStackTrace();
		}
		
	}

	
	/*
	 Closing the Index Writer after processing all files.
	 */
	public void closeIndexWriter()
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
	
	/* 
	 * take the parsed results to form the document with the different fields 
	 */
	public Document getDocument(String url, String title, String content) throws IOException {
		// TASK: Method to get a lucene document from HTML files in folder
		// create various types of fields
		Document document = new Document();
		
		// index file contents
		Field contentField = new StringField(Fieldz.title.name(), title, Store.YES);
		// index file name
		Field fileNameField = new TextField(Fieldz.content.name(), content, Store.YES);
		// index file path
		Field filePathField = new StringField(Fieldz.path.name(),url, Store.YES);
		// index file summary
		//Field summaryField = new TextField(Fieldz.summary.name(), result.getSummary(), Store.YES);

		document.add(contentField);
		document.add(fileNameField);
		document.add(filePathField);
		//document.add(summaryField);

		return document;
	}
	
	/*
	 take all .html files in the given folderlist and add them to the Index writer
	 */
	public void AddFilestoIndex(String[] URLS)  
	{	// foreach URL in List add content do Index
		for (String s : URLS) 
		{		
			org.jsoup.nodes.Document Doc = null;
			// connect to URL and generate Jsoup-Document
			try{
			Doc = Jsoup.connect(s).get();
			}catch (IOException e) {
				System.out.println("Warning: "+ e);
				continue;
			}
			try 
			{	 
				//Take Jsoup-Docs title and body to form the lucene-document
				Document D = getDocument(s, Doc.title(), Doc.body().text());
				if (D != null)
					writer.addDocument(D);
			} catch (IOException e) {ErrorAndExit(e.toString());}
		}
	}

	/*
	 Creates the index reader and the index searcher. The FSDirectory must exist at this time
	 */
	public void CreateIndexReaderAndSearcher() 
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

	/*
	 Sets the similarity method due to the user input to VS or OkapiBM25
	 */
	public void SetSimilarityMethod() 
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
	
	/*
	 Basic search method, takes an integer as input which tells how many hits should be chosen
	 creates an easy to read output data object (SearchResult)
	 */
	public void Search(int bestN) 
	{	
		ScoreDoc[] hits = null;
		Query querry = null;
		try 
		{
			querry = MultiFieldQueryParser.parse(new String[]{this.querryString, this.querryString}, new String[]{Fieldz.content.name(),Fieldz.title.name()}, this.AnalyzerIncludingStemmer);
			//Qtitle = new QueryParser(Fieldz.title.name(), this.AnalyzerIncludingStemmer).parse(this.querryString);
			//Qbody = new QueryParser(Fieldz.content.name(), this.AnalyzerIncludingStemmer).parse(this.querryString);
			System.out.println("Searching for \"" + this.querryString + "\"(stemmed)");
		} 
		catch (ParseException e1) {ErrorAndExit(e1.toString());}
		
		try 
		{
			TopDocs docs = ISearch.search(querry, bestN);
			System.out.println("total hits: " + docs.totalHits);
			System.out.println("Ranking the best 10 documents...");
	        hits = docs.scoreDocs;       
		} 
		catch (IOException e2) {ErrorAndExit(e2.toString());}
		
		
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

	/*
	 simple method to print the results (best N hits due to the query)
	 */
	public void PrintResults() 
	{
		for (int i=0; i  < SearchOutput.size(); i++)
		{
			String w = SearchOutput.get(i).toString();
			System.out.println(w);
			//TODO: save results into file (nice 2 have)
		}	
	}

	/*
	 just a little helper to test if the SearchObject is created correctly
	 */
	public String toString() 
	{
		String temp = "";
		for (String s : this.querryArray) {
			temp += s;
			temp += ",";
		}
		return (this.URLArray + "\n" + this.indexPath + "\n"
				+ this.ranking.toString() + "\n" + temp);
	}

	/*
	 own error method to catch errors while debugging
	 */
	public static void ErrorAndExit(String errorMsg) 
	{
		System.out.println("ERROR:" + errorMsg);
		System.out.println("Press any key to close...");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine(); //so the program stays open if you want to analyse the output
		System.exit(0);
	}
}
