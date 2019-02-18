package mono.cecil;

public enum ManifestResourceAttributes
{
	VisibilityMask( 0x0007 ),
	Public( VisibilityMask, 0x0001 ),    // The resource is exported from the Assembly
	Private( VisibilityMask, 0x0002 )     // The resource is private to the Assembly
	;

	private final int value;
	private final ManifestResourceAttributes masked;

	ManifestResourceAttributes( int value )
	{
		this.value = value;
		masked = null;
	}

	ManifestResourceAttributes( ManifestResourceAttributes masked, int value )
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
