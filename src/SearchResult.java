
public class SearchResult 
{
	// rank, title and summary, relevance score and path
	private String rank;
	private String title;
	private String summary;
	private String score;
	private String path;
	
	public SearchResult(int ran, String tit, float sco, String pat)
	{
		this.rank = Integer.toString(ran);
		this.title = tit;
		this.summary = sum;
		this.score = Float.toString(sco);
		this.path = pat;
	}
	
	public String toString()
	{
		String temp = "";
		temp += rank +". ";
		temp += title + ", \"";
		temp += summary + "\"";
		temp += "Score:" + score ;
		temp += "(" + path + ")\n";
		return temp;
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

	public String getScore() {
		return score;
	}

	public String getPath() {
		return path;
	}

}
