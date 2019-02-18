package mono.cecil;

public enum PInvokeAttributes
{
	NoMangle( 0x0001 ),    // PInvoke is to use the member name as specified

	// Character set
	CharSetMask( 0x0006 ),
	CharSetNotSpec( CharSetMask, 0x0000 ),
	CharSetAnsi( CharSetMask, 0x0002 ),
	CharSetUnicode( CharSetMask, 0x0004 ),
	CharSetAuto( CharSetMask, 0x0006 ),

	SupportsLastError( 0x0040 ),    // Information about target function. Not relevant for fields

	// Calling convetion
	CallConvMask( 0x0700 ),
	CallConvWinapi( CallConvMask, 0x0100 ),
	CallConvCdecl( CallConvMask, 0x0200 ),
	CallConvStdCall( CallConvMask, 0x0300 ),
	CallConvThiscall( CallConvMask, 0x0400 ),
	CallConvFastcall( CallConvMask, 0x0500 ),

	BestFitMask( 0x0030 ),
	BestFitEnabled( BestFitMask, 0x0010 ),
	BestFitDisabled( BestFitMask, 0x0020 ),

	ThrowOnUnmappableCharMask( 0x3000 ),
	ThrowOnUnmappableCharEnabled( ThrowOnUnmappableCharMask, 0x1000 ),
	ThrowOnUnmappableCharDisabled( ThrowOnUnmappableCharMask, 0x2000 );

	private final int value;
	private final PInvokeAttributes masked;

	PInvokeAttributes( int value )
	{
		this.value = value;
		masked = null;
	}

	PInvokeAttributes( PInvokeAttributes masked, int value )
	{
		this.masked = masked;
		this.value = value;
	}

	public boolean isSet( int value )
	{
		if( masked == null )
			return ( value & this.value ) != 0;

		return ( value & masked.getValue() ) == this.value;
	}

	public int getValue()
	{
		return value;
	}

	public int set( boolean value, int target )
	{
		if( masked == null )
		{
			if( value )
				return target | this.value;

			return target & ~this.value;
		}

		if( value )
			return ( target & ~masked.getValue() ) | this.value;

		return target & ~( this.value & masked.getValue() );
	}
}
