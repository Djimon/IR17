import java.io.Reader;
//Friedrich added the following
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
//Friedrich added the following
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
//Friedrich added the following
import org.apache.lucene.util.Constants;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.DirectoryReader;
//Friedrich added the following
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
//conflict
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
//import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

enum rankingModel {
	VectorSpace, OkapiBM25, invalid
}

/*
 * HINWEIS/ INFOS: laut Aufgabenstellung soll das Programm folgendermaßen
 * afugerufen werden java -jar IR_P01.jar [pathtodocumentfolder]
 * [pathtoindexfolder] [VS/OK] [query] Ausführung mit diesen parametern
 * funktioniert bereits, sogar mit beliebig langer query siehe IR_P01.jar im
 * Ordner (release)
 * 

 * Aufgabenstellung:  
  	Lucene is an open source search library that provides
	standard functionality for analyzing, indexing, and searching text-based documents.  The
	following criteria have to be met by your Information Retrieval system.
	
	[Friedrich] Using  Lucene,  parse  and  index HTML documents  that  a  given  folder  and  its subfolders 
		contain.  List all parsed files!!!!
	[Friedrich] Consider the English language and use a stemmer for it (e.g. Porter Stemmer)
	[Kilian] Select an available search index or create a new one (if not available in the chosen directory)
	[Kilian] Make possible for the user to choose the ranking model, Vector Space Model (VS) 
		or Okapi BM25 (OK) -> beinhaltet auch das Berechnen der einzelnen rankings
	[?] Print  a  ranked  list  of  relevant  articles  given  a  search  query.   The  output  should
		contain 10 most relevant documents with their rank, title and summary, relevance score and path.
	[Chris] Search   multiple   fields   concurrently   (multifield   search): not   only   search   the
		document’s text (body tag), but also its title
	
	[X] Create  a  jar-File  named  IR_P01.jar. 
	[X] It should process the input: 
		> java -jar IRP01.jar [pathtodocumentfolder] [pathtoindexfolder] [VS/OK] [query]		
 * 

 * FERTIGE TEILAUFGABEN MIT NEU COMPILIERTER .JAR TESTEN UND MIT 'X' ABHAKEN!!
 */

public class LuceneTest {

	private String docPath;
	private String indexPath;
	// Friedrich added the following:
	private static final String fieldPath = "path";
	public static final String fieldContents = "contents";

	private rankingModel ranking;
	private String querryArray[];
	private ArrayList<Document> docList = new ArrayList<Document>();
	private ArrayList<Document> stemmedDocList = new ArrayList<Document>();
	
	public LuceneTest (String docs, String index, rankingModel rank, ArrayList<String> query) 
	{
		this.querryArray = new String[query.size()];
		for (int i = 0; i < query.size(); i++) {
			this.querryArray[i] = query.get(i);
			// System.out.println("get "+query.get(i));
		}
		this.docPath = docs;
		this.indexPath = index;
		this.ranking = rank;
	}

	private void RunPorterStemmer() {

		// I bims 1 nicer Porter Stemmer
		// TODO: run porter Stemmer on all given docs in docList and fill the
		// stemmedDocList with the stemmed words
		// this.docList
	}

	private void SelectIndex() {
		// TODO: Select index from given folder, if not available, create the
		// index from the stemmedDocList
	}

	
	// Friedrich added a method
	private Document getDocument(File file, jsoupResultStrings result) throws IOException {
		// TASK: Method to get a lucene document from HTML filoes in folder
		// create various types of fields
		Document document = new Document();
		
		// index file contents
		Field contentField = new TextField("content", result.getBody(), Store.YES);
		// index file name
		Field fileNameField = new TextField("name", result.getTitle(), Store.YES);
		// index file path
		Field filePathField = new TextField("path",file.getCanonicalPath(), Store.YES);

		document.add(contentField);
		document.add(fileNameField);
		document.add(filePathField);

		return document;
	}
	
private jsoupResultStrings getJsoupStrings(File file){	
		// create documents via JSOUP(jsoup.org) 
		org.jsoup.nodes.Document doc = Jsoup.parse(docPath); 
		 //only one document import (jsoup collides with lucene) 
		 //ouput the title and the body content 
		 jsoupResultStrings result = new jsoupResultStrings(doc.title(),doc.body().text());
return result;
	}

	private void ParseDocsinGivenFolder() {

		File folder = new File(docPath);
		File[] listOfFiles = folder.listFiles();
		showFiles(listOfFiles); 
		// TODO: read and show each HTML document and save its contents as new Document()						

		// Friedrich added the following:
		Analyzer analyzer = new StandardAnalyzer();
		// create an index directory and configure it with the analyzer instance
		// Directory index = FSDirectory.open(new File(indexPath));

		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		// create a file to get directory

		// indexwriterconfig
		// IndexWriter indexWriter = new IndexWriter(index, config);

		
		// TODO: add header and body field to the new Document (doc.Add(new
		// IndexableField) ???

		// Document n = new Document();
		// n.add(???);
	}

	private void showFiles(File[] files) {
		// TODO: Parse the whole folder (including subfolders) and list the
		// html-documents
		for (File file : files) {
			if (file.isDirectory()) {
				//System.out.println("Directory: " + file.getName());
				showFiles(file.listFiles());  //recursion
			} else {
				System.out.println("File: " + file.getName());
				
				try {
					getDocument(file, getJsoupStrings(file));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("Error: "+e);
				}

			}
		}
	}

	private void StemmQuery() {
		// do Stuff

	}

	private void Search() {

		/*
		 * TODO: Do the actual searching with: - calculating tf-idf - relevance
		 * score - ranking - konkurierende Suche in Titel und Textkörper
		 * (mulrifield search)
		 */
	}

	private void PrintResults() {
		/*
		 * TODO: print best 10 documents with: -> rank, title, summary,
		 * relevance, score, path
		 */
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

	// ***************************************************** \\
	// ****************** MAIN METHODEN ****************** \\
	// ***************************************************** \\

	public static void main(String[] args) {

		String docFolder = null;
		String indexFolder = null;
		rankingModel ranking = rankingModel.invalid;
		ArrayList<String> query = new ArrayList<String>();
		LuceneTest SearchObject = null;

		if (args.length >= 3) {
			docFolder = args[0];
			indexFolder = args[1];
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
			System.out.println(SearchObject.toString());

			// **********************************
			// TODO: COMPLETE SEARCH LOGIC HERE!!
			// **********************************
			SearchObject.ParseDocsinGivenFolder();
			SearchObject.RunPorterStemmer();

			// SearchObject.closeIndex();

			SearchObject.SelectIndex();
			SearchObject.StemmQuery();
			SearchObject.Search();
		}
	}

	private static void ErrorAndExit(String errorMsg) {
		System.out.println("ERROR:" + errorMsg);
		System.out.println("Press any key to close...");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		System.exit(0);
	}
}
