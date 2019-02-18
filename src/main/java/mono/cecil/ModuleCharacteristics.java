package mono.cecil;

public enum ModuleCharacteristics
{
	HighEntropyVA( 0x0020 ),
	DynamicBase( 0x0040 ),
	NoSEH( 0x0400 ),
	NXCompat( 0x0100 ),
	AppContainer( 0x1000 ),
	TerminalServerAware( 0x8000 );

	private final int mask;

	ModuleCharacteristics( int mask )
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
