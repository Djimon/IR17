 import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.FilesystemResourceLoader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;


import java.nio.file.*;

enum rankingModel {VectorSpace,OkapiBM25,invalid}

/* HINWEIS/ INFOS:
 * laut Aufgabenstellung soll das Programm folgendermaßen afugerufen werden
 * java -jar IRP01.jar [pathtodocumentfolder] [pathtoindexfolder] [VS/OK] [query]
 * Ausführung mit diesen parametern funktioniert bereits, sogar mit beliebig langer query
 * siehe IRP01.jar im Ordner (release)
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

public class LuceneTest 
{	
	private String docPath;
	private String indexPath;
	private rankingModel ranking;
	private String querryArray[];
	private ArrayList<Document> docList = new ArrayList<Document>();
	private ArrayList<Document> stemmedDocList = new ArrayList<Document>();
	// Add all the variables you need ;)
	
	public LuceneTest(String docs, String index, rankingModel rank, ArrayList<String> query)
	{
		this.querryArray = new String[query.size()];
		for (int i = 0; i< query.size(); i++)
		{
			this.querryArray[i] = query.get(i);
			//System.out.println("get "+query.get(i));
		}
		this.docPath = docs;
		this.indexPath = index;
		this.ranking = rank;		
	}
	
	private void SelectIndex() 
	{
		Path path = FileSystems.getDefault().getPath(indexPath, "invertedIndex.idx");
		
		/*
		 If file already exists, index has already been created. Otherwise a new file is created which will contain the inverted index.
		 */
		if(!java.nio.file.Files.exists(path))
		{
			/*
			 Create File.
			 */
			File file = new File(path.toString());
			file.mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("Creating new file was unsuccessful. File already exists");
				e.printStackTrace();
			}
			
			/*
			 Actual creation of the inverted index.
			 */
			ArrayList<String> index = new ArrayList<String>();
					//TODO: create index
			
			for (Document doc : stemmedDocList) {
				
				
			}
			
			
			/*
			 Writing the inverted index into the file.
			 */
			BufferedWriter output = null;
			String temp = "";
			String[] temp2 = new String[index.size()];
			temp2 = index.toArray(temp2);
			for (int i = 0; i < index.size(); i++)
			{
				temp += temp2[i];
			}
	        try {
	            output = new BufferedWriter(new FileWriter(file));
	            output.write(temp);
	        } catch ( IOException e ) {
	            e.printStackTrace();
	        } 
	        try {
				output.close();
			} catch (IOException e) {
				System.out.println("BufferedWriter can't be closed, because it wasn't initialized.");
				e.printStackTrace();
			}
		}
		
		// TODO: Select index from given folder, if not available, create the index from the stemmedDocList		
	}

	private void RunPorterStemmer() 
	{
		
		// I bims 1 nicer Porter Stemmer
		// TODO: run porter Stemmer on all given docs in docList and fill the stemmedDocList with the stemmed words
		// this.docList	
	}

	private void ParseDocsinGivenFolder() 
	{
		// TODO: Parse the whole folder (including subfolders) an list the html-documents
		// TODO: read each HTML document and save its contents as new Document()
		// TODO: add header and body field to the new Document (doc.Add(new IndexableField) ???
		
		Document n = new Document();		
		//n.add(???);	
	}
	
	private void StemmQuery() 
	{
		// do Stuff
			
	}
	
	private void Search() 
	{
		/* TODO: Do the actual searching with:
		 * - calculating tf-idf
		 * - relevance score
		 * - ranking
		 * - konkurierende Suche in Titel und Textkörper (mulrifield search)
		 */		
	}
	
	private void PrintResults()
	{
		/*TODO: print best 10 documents with:
		 * -> rank, title, summary, relevance, score, path
		 */
	}

	
	
	
	public String toString()
	{
		String temp = "";
		for (String s : this.querryArray)
		{
			temp += s;
			temp += ",";
		}
		return (this.docPath+"\n"+this.indexPath+"\n"+this.ranking.toString()+"\n"+temp);
	}

	
	// ***************************************************** \\
	// ******************  MAIN METHODEN  ****************** \\
	// ***************************************************** \\
	
	public static void main(String[] args) 
	{
		String docFolder = "";
		String indexFolder = "";
		rankingModel ranking = rankingModel.invalid;
		ArrayList<String> query = new ArrayList<String>();
		LuceneTest SearchObject = null;
		
		if (args.length >= 3)
		{
			docFolder = args[0];
			indexFolder = args[1];
			ranking = (args[2].equals("VS") ? rankingModel.VectorSpace: args[2].equals("OK") ? rankingModel.OkapiBM25: rankingModel.invalid); 
		}			
		else
		{
			ErrorAndExit("arguments missing - programm needs at least 4");			
		}
		
		
		if (ranking == rankingModel.invalid)
		{
			ErrorAndExit("entered invalid ranking model(" + args[2] + ")");
		}
		else
		{		
			for (int i = 3; i< args.length; i++)
			{
				query.add(args[i]);
				//System.out.println("arg "+i +": " + args[i]);
			}	
			SearchObject = new LuceneTest(docFolder, indexFolder, ranking, query);
			System.out.println("Successfully instantiated SearchObject:");
			System.out.println(SearchObject.toString());
			
			//**********************************
			//TODO: COMPLETE SEARCH LOGIC HERE!!
			//**********************************
			SearchObject.ParseDocsinGivenFolder();
			SearchObject.RunPorterStemmer();
			SearchObject.SelectIndex();
			SearchObject.StemmQuery();
			SearchObject.Search();
		}		
	}

	private static void ErrorAndExit(String errorMsg) 
	{
		System.out.println("ERROR:"+ errorMsg);
		System.out.println("Press any key to close...");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		System.exit(0);
	}
}
