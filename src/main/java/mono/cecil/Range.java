package mono.cecil;

@SuppressWarnings( "unused" )
public class Range
{
	private int start;
	private int length;

	public Range()
	{
	}

	public Range( int start, int length )
	{
		this.start = start;
		this.length = length;
	}

	public int getStart()
	{
		return start;
	}

	public void setStart( int start )
	{
		this.start = start;
	}

	public int getLength()
	{
		return length;
	}

	public void setLength( int length )
	{
		this.length = length;
	}
}
