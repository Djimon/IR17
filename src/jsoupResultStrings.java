
public class jsoupResultStrings 
{
	private  String title="";
	private  String body="";
	private String summary="";
	
	public jsoupResultStrings (String title, String body, String summ)
	{
		this.title=title;
		this.body=body;
		this.summary = summ;
	}
	
	public String getTitle() 
	{
		return this.title;
	}
	
	public String getBody() 
	{
		return this.body;
	}
	
	public String getSummary()
	{
		return this.summary;
	}
}
