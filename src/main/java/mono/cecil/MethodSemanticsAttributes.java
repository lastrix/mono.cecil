package mono.cecil;

public enum MethodSemanticsAttributes
{
	None( 0x0000 ),
	Setter( 0x0001 ),    // Setter for property
	Getter( 0x0002 ),    // Getter for property
	Other( 0x0004 ),    // Other method for property or event
	AddOn( 0x0008 ),    // AddOn method for event
	RemoveOn( 0x0010 ),    // RemoveOn method for event
	Fire( 0x0020 )     // Fire method for event
	;

	private final int mask;

	MethodSemanticsAttributes( int mask )
	{
		this.mask = mask;
	}

	public boolean isSet( int value )
	{
		return ( value & mask ) != 0;
	}

	public int getMask()
	{
		return mask;
	}

	public int set( boolean value, int target )
	{
		if( value )
			return target | mask;

		return target & ~mask;
	}

	public static MethodSemanticsAttributes getByCode( int code )
	{
		for( MethodSemanticsAttributes item : values() )
		{
			if( item.getMask() == code )
				return item;
		}

		throw new IllegalArgumentException( "Unknown code: " + code );
	}
}
