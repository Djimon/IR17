import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class URLcrawlerAndIndexer {

	// global variables
	private static List<PrintObject> URLindexList = new ArrayList<PrintObject>();
	// private static List<org.jsoup.nodes.Document> listOfHTMLfiles = new
	// ArrayList<org.jsoup.nodes.Document>(); // initalize
	// Documents
	private static int maxLevel;

	private static String indexDirectory;

	/*
	 * public URLcrawlerAndIndexer(){ URLindexList = new
	 * ArrayList<PrintObject>(); }
	 */
/*
	public static void main(String[] args) throws IOException {
		/*
		 * // check for correct number of arguments if (args.length < 4) {
		 * System.out
		 * .println("The entered request had no sufficient amount of arguments"
		 * ); System.exit(-1); }
		 * 
		 * String seedURL = args[0]; maxLevel = Integer.parseInt(args[1]); File
		 * indexFolder = new File(args[2]);
		 * 
		 * List<String> query = new ArrayList<String>(); for (int i = 3; i <
		 * args.length; i++) { query.add(args[i]); }
		 * 
		 * // 1 zu1 Directory indexDir = null; try { indexDir =
		 * FSDirectory.open(indexFolder.toPath()); } catch (IOException e) {
		 * System.out.println("The entered index path resulted in an error: " +
		 * e.getMessage()); System.exit(-1); } indexDirectory =
		 * indexDir.toString();
		 */
/*
		String seedURL =  "http://www.dke-research.de/findke/en/Studies/Courses/WS+2017_2018-p-1064.html";
		String seedURL = "http://www.mkyong.com/";
		maxLevel = 2;
		indexDirectory = "C:/Users/admin/Desktop/WS_17_18/Information_Retrieval/P02";
*/

//	}
	
	public List<PrintObject> run (String seed, String indexDir, int maxDepth) throws IOException{
		maxLevel=maxDepth;
		indexDirectory=indexDir;
		
		// recursively crawl HTML documents, starting at a seed/base URL to a
		// previously set depth level
		crawlAndAddURLtoList(seed, 0);

		// print URL and Depth
		printURLtoTXT(URLindexList);
		
		return URLindexList;
	}

	private static void crawlAndAddURLtoList(String URL, int depth)
			throws IOException {
		int depth_ = depth;
		// check for cycles
		boolean duplicate = false;
		for (int i = 0; i < URLindexList.size(); i++)
			if (URLindexList.get(i).getURL().equals(URL)) {
				duplicate = true;
			}

		System.out.println(">> Depth: " + depth + " [" + URL + "]");
		// check for maxLevel
		if (duplicate == false && depth <= maxLevel) {
			try {
				URL = normalize(URL);
				// add URL and Index to PrintObjectList
				PrintObject po = new PrintObject(URL, depth);
				if (!URLindexList.contains(po))
					URLindexList.add(po);
				System.out.println(URLindexList.size());
				// try to connect with the current (seed-) URL and retrieve HTML
				// document
				org.jsoup.nodes.Document doc = Jsoup.connect(URL).get();

				// add HTML Document to list
				// listOfHTMLfiles.add(doc);

				// retrieve all links from the current HTML Document
				Elements links = doc.select("a[href]");
				depth_++;
				// recursively add all links to the URLlist if it is not in the
				// list
				// already
				for (Element page : links) {
					crawlAndAddURLtoList(page.attr("abs:href"), depth_);
				}

			} catch (IOException e) {
				System.err.println("URL: " + " error: " + e);
			}
		} else {
			return;
		}
	}

	private static String normalize(String URL) {
		String URL_norm = URL.toLowerCase();
		if (URL_norm.endsWith("/"))
		// replace string by a string that is one character shorter, thus
		// dleting the trailing slash
		{
			URL_norm = URL_norm.substring(0, URL_norm.length() - 1);
		}
		//ignore anker by looking if # is contained --> anker-separator
		if (URL_norm.contains("#"))
			//divide string and only keep the first part, thus avoiding the anker-part
		{URL_norm = URL_norm.split("#")[0];}
		return URL_norm;

	}

	private static void printURLtoTXT(List<PrintObject> URL) throws IOException {
		String path = indexDirectory + File.separator + "pages.txt";

		FileWriter fw = new FileWriter(path);

		for (int i = 0; i < URLindexList.size(); i++) {
			// System.out.println("hier" + URLindexList.size());
			fw.write(URLindexList.get(i).getURL() + ";"
					+ URLindexList.get(i).getLevel() + "\n");
		}
		fw.close();
	}
}
