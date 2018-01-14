
public class PrintObject {
	
private String URL;
private int level;

public PrintObject (String url, int lvl){
	this.setLevel(lvl);
	this.setURL(url);
}

public int getLevel() {
	return level;
}
public void setLevel(int level) {
	this.level = level;
}

public String getURL() {
	return URL;
}

public void setURL(String uRL) {
	URL = uRL;
}

}
