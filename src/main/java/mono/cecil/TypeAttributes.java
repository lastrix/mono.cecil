package mono.cecil;

public enum TypeAttributes
{
	// Visibility attributes
	VisibilityMask( 0x00000007 ),    // Use this mask to retrieve visibility information
	NotPublic( VisibilityMask, 0x00000000 ),    // Class has no public scope
	Public( VisibilityMask, 0x00000001 ),    // Class has public scope
	NestedPublic( VisibilityMask, 0x00000002 ),    // Class is nested with public visibility
	NestedPrivate( VisibilityMask, 0x00000003 ),    // Class is nested with private visibility
	NestedFamily( VisibilityMask, 0x00000004 ),    // Class is nested with family visibility
	NestedAssembly( VisibilityMask, 0x00000005 ),    // Class is nested with assembly visibility
	NestedFamANDAssem( VisibilityMask, 0x00000006 ),    // Class is nested with family and assembly visibility
	NestedFamORAssem( VisibilityMask, 0x00000007 ),    // Class is nested with family or assembly visibility

	// Class layout attributes
	LayoutMask( 0x00000018 ),    // Use this mask to retrieve class layout information
	AutoLayout( LayoutMask, 0x00000000 ),    // Class fields are auto-laid out
	SequentialLayout( LayoutMask, 0x00000008 ),    // Class fields are laid out sequentially
	ExplicitLayout( LayoutMask, 0x00000010 ),    // Layout is supplied explicitly

	// Class semantics attributes
	ClassSemanticMask( 0x00000020 ),    // Use this mask to retrieve class semantics information
	Class( ClassSemanticMask, 0x00000000 ),    // Type is a class
	Interface( ClassSemanticMask, 0x00000020 ),    // Type is an interface

	// Special semantics in addition to class semantics
	Abstract( 0x00000080 ),    // Class is abstract
	Sealed( 0x00000100 ),    // Class cannot be extended
	SpecialName( 0x00000400 ),    // Class name is special

	// Implementation attributes
	Import( 0x00001000 ),    // Class/Interface is imported
	Serializable( 0x00002000 ),    // Class is serializable
	WindowsRuntime( 0x00004000 ),    // Windows Runtime type

	// String formatting attributes
	StringFormatMask( 0x00030000 ),    // Use this mask to retrieve string information for native interop
	AnsiClass( StringFormatMask, 0x00000000 ),    // LPSTR is interpreted as ANSI
	UnicodeClass( StringFormatMask, 0x00010000 ),    // LPSTR is interpreted as Unicode
	AutoClass( StringFormatMask, 0x00020000 ),    // LPSTR is interpreted automatically

	// Class initialization attributes
	BeforeFieldInit( 0x00100000 ),    // Initialize the class before first static field access

	// Additional flags
	RTSpecialName( 0x00000800 ),    // CLI provides 'special' behavior), depending upon the name of the Type
	HasSecurity( 0x00040000 ),    // Type has security associate with it
	Forwarder( 0x00200000 )   // Exported type is a type forwarder
	;

	private final int value;
	private final TypeAttributes masked;

	TypeAttributes( int value )
	{
		masked = null;
		this.value = value;
	}

	TypeAttributes( TypeAttributes masked, int value )
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

	public static TypeAttributes getByCode( int code )
	{
		for( TypeAttributes attributes : values() )
		{
			if( attributes.getValue() == code )
				return attributes;
		}
		throw new IllegalArgumentException( "Unsupported arch: " + code );
	}
}
