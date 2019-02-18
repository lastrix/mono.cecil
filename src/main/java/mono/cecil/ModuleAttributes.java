package mono.cecil;

public enum ModuleAttributes
{
	ILOnly( 1 ),
	Required32Bit( 2 ),
	StrongNameSigned( 8 ),
	Preferred32Bit( 0x00020000 );

	private final int mask;

	ModuleAttributes( int mask )
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
