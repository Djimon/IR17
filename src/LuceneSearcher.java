import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LuceneSearcher 
{
	//Main for executing the .jar fiel correctly
	public static void main(String[] args) throws IOException 
	{
		//java -jar IRP_01.jar [pathtodocumentfolder] [pathtoindexfolder] [VS/OK] [query]
		String seedURL = null;
		String indexFolder = null;
		int depth = 0;
		rankingModel ranking = rankingModel.invalid;
		ArrayList<String> query = new ArrayList<String>();
		SearchObject SearchObject = null;
		// [seed URL] [crawl depth] [pathtoindexfolder] [query]

		if (args.length >= 3) {
			seedURL = args[0];
			depth = Integer.parseInt(args[1]);
			indexFolder = args[2];
			if (!new File(indexFolder).isDirectory())
				SearchObject.ErrorAndExit("Argument 2 is no directory:" + indexFolder);
			ranking = rankingModel.VectorSpace;
		} else {
			SearchObject.ErrorAndExit("arguments missing - programm needs at least 4");
		}

		for (int i = 3; i < args.length; i++) {
			query.add(args[i]);

			SearchObject = new SearchObject(seedURL, indexFolder, ranking, query);
			System.out.println("Successfully instantiated SearchObject:");
			//System.out.println(SearchObject.toString());

			SearchObject.SetSimilarityMethod();

			SearchObject.SelectIndex();
			SearchObject.CreateIndexReaderAndSearcher();
			
			SearchObject.Search(10); // the 10 best results
			SearchObject.PrintResults();
		}
	}
	
}
