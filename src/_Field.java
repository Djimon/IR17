import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;

// *******************************************************************
// Platzhalter, falls nötig, blicke noch nicht ganz durch Lucene durch
// *******************************************************************
public class _Field implements IndexableField
{
	private String name = "";
	private String content = "";
	
	public _Field(String name, String content)
	{
		this.name = name;
		this.content = content;
	}
	
	
	@Override
	public String name() {
		
		return this.name;
	}

	@Override
	public IndexableFieldType fieldType() {
		// do Stuff
		return null;
	}

	@Override
	public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse) {
		// do Stuff
		return null;
	}

	@Override
	public BytesRef binaryValue() {
		// do Stuff
		return null;
	}

	@Override
	public String stringValue() {
		// do Stuff
		return null;
	}

	@Override
	public Reader readerValue() {
		// do Stuff
		return null;
	}

	@Override
	public Number numericValue() {
		// do Stuff
		return null;
	}
	

}
