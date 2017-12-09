
public class SearchResult 
{
	// rank, title and summary, relevance score and path
	private String rank;
	private String title;
	private String summary;
	private String relevance;
	private String score;
	private String path;
	
	public SearchResult(int ran, String tit, String sum, String rel, float sco, String pat)
	{
		this.rank = Integer.toString(ran);
		this.title = tit;
		this.summary = sum;
		this.relevance = rel;
		this.score = Float.toString(sco);
		this.path = pat;
	}

	public String getRank() {
		return rank;
	}

	public String getTitle() {
		return title;
	}

	public String getSummary() {
		return summary;
	}

	public String getRelevance() {
		return relevance;
	}

	public String getScore() {
		return score;
	}

	public String getPath() {
		return path;
	}

}