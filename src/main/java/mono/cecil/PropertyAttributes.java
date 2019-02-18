package mono.cecil;

public enum PropertyAttributes
{
	None( 0x0000 ),
	SpecialName( 0x0200 ),    // Property is special
	RTSpecialName( 0x0400 ),    // Runtime(metadata internal APIs) should check name encoding
	HasDefault( 0x1000 ),    // Property has default
	Unused( 0xe9ff )     // Reserved: shall be zero in a conforming implementation
	;

	private final int mask;

	PropertyAttributes( int mask )
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
}
