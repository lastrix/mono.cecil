package mono.cecil;

public enum AssemblyHashAlgorithm
{
	None( 0x0000 ),
	Reserved( 0x8003 ),    // MD5
	SHA1( 0x8004 );

	private final int code;

	AssemblyHashAlgorithm( int code )
	{

		this.code = code;
	}

	public static AssemblyHashAlgorithm getByCode( int code )
	{
		for( AssemblyHashAlgorithm algorithm : values() )
		{
			if( algorithm.getCode() == code )
				return algorithm;
		}

		throw new IllegalArgumentException( "Unknown code: " + code );
	}

	public int getCode()
	{
		return code;
	}
}
