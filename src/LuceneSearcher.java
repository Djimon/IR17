import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LuceneSearcher 
{
	//Main for executing the .jar fiel correctly
	public static void main(String[] args) throws IOException 
	{
		String docFolder = null;
		String indexFolder = null;
		rankingModel ranking = rankingModel.invalid;
		ArrayList<String> query = new ArrayList<String>();
		SearchObject SearchObject = null;

		if (args.length >= 3) {
			docFolder = args[0];
			indexFolder = args[1];
			if (!new File(indexFolder).isDirectory())
				SearchObject.ErrorAndExit("Argument 2 is no directory:" + indexFolder);
			ranking = (args[2].equals("VS") ? rankingModel.VectorSpace
					: args[2].equals("OK") ? rankingModel.OkapiBM25
							: rankingModel.invalid);
		} else {
			SearchObject.ErrorAndExit("arguments missing - programm needs at least 4");
		}

		if (ranking == rankingModel.invalid) {
			SearchObject.ErrorAndExit("entered invalid ranking model(" + args[2] + ")");
		} else {
			for (int i = 3; i < args.length; i++) {
				query.add(args[i]);
				// System.out.println("arg "+i +": " + args[i]);
			}

			SearchObject = new SearchObject(docFolder, indexFolder, ranking, query);
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
