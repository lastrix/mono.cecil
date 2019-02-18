package mono.cecil;

public enum GenericParameterAttributes
{
	VarianceMask( 0x0003 ),
	NonVariant( VarianceMask, 0x0000 ),
	Covariant( VarianceMask, 0x0001 ),
	Contravariant( VarianceMask, 0x0002 ),

	SpecialConstraintMask( 0x001c ),
	ReferenceTypeConstraint( SpecialConstraintMask, 0x0004 ),
	NotNullableValueTypeConstraint( SpecialConstraintMask, 0x0008 ),
	DefaultConstructorConstraint( SpecialConstraintMask, 0x0010 );

	private final int value;
	private final GenericParameterAttributes masked;

	GenericParameterAttributes( int value )
	{
		this.value = value;
		masked = null;
	}

	GenericParameterAttributes( GenericParameterAttributes masked, int value )
	{
		this.value = value;
		this.masked = masked;
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
