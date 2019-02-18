package mono.cecil;

public enum AssemblyAttributes
{
	PublicKey( 0x0001 ),
	SideBySideCompatible( 0x0000 ),
	Retargetable( 0x0100 ),
	WindowsRuntime( 0x0200 ),
	DisableJITCompileOptimizer( 0x4000 ),
	EnableJITCompileTracking( 0x8000 );

	private final int mask;

	AssemblyAttributes( int mask )
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
