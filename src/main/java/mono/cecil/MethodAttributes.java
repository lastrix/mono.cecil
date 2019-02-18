package mono.cecil;

public enum MethodAttributes
{
	MemberAccessMask( 0x0007 ),
	CompilerControlled( MemberAccessMask, 0x0000 ),    // Member not referenceable
	Private( MemberAccessMask, 0x0001 ),    // Accessible only by the parent type
	FamANDAssem( MemberAccessMask, 0x0002 ),    // Accessible by sub-types only in this Assembly
	Assembly( MemberAccessMask, 0x0003 ),    // Accessibly by anyone in the Assembly
	Family( MemberAccessMask, 0x0004 ),    // Accessible only by type and sub-types
	FamORAssem( MemberAccessMask, 0x0005 ),    // Accessibly by sub-types anywhere), plus anyone in assembly
	Public( MemberAccessMask, 0x0006 ),    // Accessibly by anyone who has visibility to this scope

	Static( 0x0010 ),    // Defined on type), else per instance
	Final( 0x0020 ),    // Method may not be overridden
	Virtual( 0x0040 ),    // Method is virtual
	HideBySig( 0x0080 ),    // Method hides by name+sig), else just by name

	VtableLayoutMask( 0x0100 ),    // Use this mask to retrieve vtable attributes
	ReuseSlot( VtableLayoutMask, 0x0000 ),    // Method reuses existing slot in vtable
	NewSlot( VtableLayoutMask, 0x0100 ),    // Method always gets a new slot in the vtable

	CheckAccessOnOverride( 0x0200 ),   // Method can only be overriden if also accessible
	Abstract( 0x0400 ),    // Method does not provide an implementation
	SpecialName( 0x0800 ),    // Method is special

	// Interop Attributes
	PInvokeImpl( 0x2000 ),    // Implementation is forwarded through PInvoke
	UnmanagedExport( 0x0008 ),    // Reserved: shall be zero for conforming implementations

	// Additional flags
	RTSpecialName( 0x1000 ),    // CLI provides 'special' behavior), depending upon the name of the method
	HasSecurity( 0x4000 ),    // Method has security associate with it
	RequireSecObject( 0x8000 )     // Method calls another method containing security code
	;

	private final int value;
	private final MethodAttributes masked;

	MethodAttributes( int value )
	{
		this.value = value;
		masked = null;
	}

	MethodAttributes( MethodAttributes masked, int value )
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
