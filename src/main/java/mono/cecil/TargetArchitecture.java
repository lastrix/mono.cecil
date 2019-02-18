package mono.cecil;


public enum TargetArchitecture
{
	I386( 0x014c ),
	AMD64( 0x8664 ),
	IA64( 0x0200 ),
	ARMv7( 0x01c4 );

	private final int code;

	TargetArchitecture( int code )
	{
		this.code = code;
	}

	public int getCode()
	{
		return code;
	}

	public static TargetArchitecture getByCode( int code )
	{
		for( TargetArchitecture architecture : values() )
		{
			if( architecture.getCode() == code )
				return architecture;
		}
		throw new UnsupportedArchitectureException( "Unsupported arch: " + Integer.toHexString( code ) );
	}
}
