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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BytesRef binaryValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String stringValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reader readerValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number numericValue() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
