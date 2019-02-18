package mono.cecil.metadata.rows;

public final class Row2<C1, C2>
{
	private final C1 col1;
	private final C2 col2;

	public Row2( C1 col1, C2 col2 )
	{
		this.col1 = col1;
		this.col2 = col2;
	}

	public C1 getCol1()
	{
		return col1;
	}

	public C2 getCol2()
	{
		return col2;
	}

	@SuppressWarnings( "AccessingNonPublicFieldOfAnotherObject" )
	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof Row2 ) ) return false;

		Row2<?, ?> row2 = (Row2<?, ?>)obj;

		//noinspection SimplifiableIfStatement
		if( !col1.equals( row2.col1 ) ) return false;
		return col2.equals( row2.col2 );

	}

	@Override
	public int hashCode()
	{
		int result = col1.hashCode();
		result = 31 * result + col2.hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		return '<' + col1.getClass().getName() + ", " + col2.getClass().getName() + '>';
	}
}
