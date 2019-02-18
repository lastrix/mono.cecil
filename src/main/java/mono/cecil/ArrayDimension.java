package mono.cecil;

public class ArrayDimension
{
	private Integer lowerBound;
	private Integer upperBound;

	public ArrayDimension()
	{
		this( null, null );
	}

	public ArrayDimension( Integer lowerBound, Integer upperBound )
	{
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public Integer getLowerBound()
	{
		return lowerBound;
	}

	public void setLowerBound( Integer lowerBound )
	{
		this.lowerBound = lowerBound;
	}

	public Integer getUpperBound()
	{
		return upperBound;
	}

	public void setUpperBound( Integer upperBound )
	{
		this.upperBound = upperBound;
	}

	public boolean isSized()
	{
		return lowerBound != null || upperBound != null;
	}

	@Override
	public String toString()
	{
		return isSized() ? lowerBound + "..." + upperBound : "";
	}
}
