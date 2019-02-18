package mono.cecil;

public enum MethodCallingConvention
{
	Default( 0x0 ),
	C( 0x1 ),
	StdCall( 0x2 ),
	ThisCall( 0x3 ),
	FastCall( 0x4 ),
	VarArg( 0x5 ),
	Generic( 0x10 );

	private final int code;

	MethodCallingConvention( int code )
	{
		this.code = code;
	}

	public int getCode()
	{
		return code;
	}

	public static MethodCallingConvention getByCode( int code )
	{
		for( MethodCallingConvention convention : values() )
		{
			if( convention.getCode() == code )
				return convention;
		}

		throw new IllegalArgumentException( "Unknown code: " + code );
	}
}
