package mono.cecil.metadata.rows;

public final class Row3<C1, C2, C3>
{
	private final C1 col1;
	private final C2 col2;
	private final C3 col3;

	public Row3( C1 col1, C2 col2, C3 col3 )
	{
		this.col1 = col1;
		this.col2 = col2;
		this.col3 = col3;
	}

	@SuppressWarnings( "AccessingNonPublicFieldOfAnotherObject" )
	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof Row3 ) ) return false;

		Row3<?, ?, ?> row3 = (Row3<?, ?, ?>)obj;

		if( !col1.equals( row3.col1 ) ) return false;
		//noinspection SimplifiableIfStatement
		if( !col2.equals( row3.col2 ) ) return false;
		return col3.equals( row3.col3 );

	}

	public C1 getCol1()
	{
		return col1;
	}

	public C2 getCol2()
	{
		return col2;
	}

	public C3 getCol3()
	{
		return col3;
	}

	@Override
	public int hashCode()
	{
		int result = col1.hashCode();
		result = 31 * result + col2.hashCode();
		result = 31 * result + col3.hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		return '<' +
				col1.getClass().getName() + ", " +
				col2.getClass().getName() + ", " +
				col3.getClass().getName() +
				'>';
	}
}
