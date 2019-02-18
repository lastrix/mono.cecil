package mono.cecil;

public enum EventAttributes
{
	None( 0x0000 ),
	SpecialName( 0x0200 ),    // Event is special
	RTSpecialName( 0x0400 )     // CLI provides 'special' behavior), depending upon the name of the event
	;

	private final int mask;

	EventAttributes( int mask )
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
