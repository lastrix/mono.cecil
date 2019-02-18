package mono.cecil;

public enum FieldAttributes
{
	FieldAccessMask( 0x0007 ),
	CompilerControlled( FieldAccessMask, 0x0000 ),    // Member not referenceable
	Private( FieldAccessMask, 0x0001 ),    // Accessible only by the parent type
	FamANDAssem( FieldAccessMask, 0x0002 ),    // Accessible by sub-types only in this assembly
	Assembly( FieldAccessMask, 0x0003 ),    // Accessible by anyone in the Assembly
	Family( FieldAccessMask, 0x0004 ),    // Accessible only by type and sub-types
	FamORAssem( FieldAccessMask, 0x0005 ),    // Accessible by sub-types anywhere), plus anyone in the assembly
	Public( FieldAccessMask, 0x0006 ),    // Accessible by anyone who has visibility to this scope field contract attributes

	Static( 0x0010 ),    // Defined on type), else per instance
	InitOnly( 0x0020 ),    // Field may only be initialized), not written after init
	Literal( 0x0040 ),    // Value is compile time constant
	NotSerialized( 0x0080 ),    // Field does not have to be serialized when type is remoted
	SpecialName( 0x0200 ),    // Field is special

	// Interop Attributes
	PInvokeImpl( 0x2000 ),    // Implementation is forwarded through PInvoke

	// Additional flags
	RTSpecialName( 0x0400 ),    // CLI provides 'special' behavior), depending upon the name of the field
	HasFieldMarshal( 0x1000 ),    // Field has marshalling information
	HasDefault( 0x8000 ),    // Field has default
	HasFieldRVA( 0x0100 )     // Field has RVA
	;

	private final int value;
	private final FieldAttributes masked;

	FieldAttributes( int value )
	{
		this.value = value;
		masked = null;
	}

	FieldAttributes( FieldAttributes masked, int value )
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
