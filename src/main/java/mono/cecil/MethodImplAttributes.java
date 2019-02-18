package mono.cecil;

public enum MethodImplAttributes
{
	CodeTypeMask( 0x0003 ),
	IL( CodeTypeMask, 0x0000 ),    // Method impl is CIL
	Native( CodeTypeMask, 0x0001 ),    // Method impl is native
	OPTIL( CodeTypeMask, 0x0002 ),    // Reserved: shall be zero in conforming implementations
	Runtime( CodeTypeMask, 0x0003 ),    // Method impl is provided by the runtime

	ManagedMask( 0x0004 ),    // Flags specifying whether the code is managed or unmanaged
	Unmanaged( ManagedMask, 0x0004 ),    // Method impl is unmanaged), otherwise managed
	Managed( ManagedMask, 0x0000 ),    // Method impl is managed

	// Implementation info and interop
	ForwardRef( 0x0010 ),    // Indicates method is defined; used primarily in merge scenarios
	PreserveSig( 0x0080 ),    // Reserved: conforming implementations may ignore
	InternalCall( 0x1000 ),    // Reserved: shall be zero in conforming implementations
	Synchronized( 0x0020 ),    // Method is single threaded through the body
	NoOptimization( 0x0040 ),    // Method is not optimized by the JIT.
	NoInlining( 0x0008 )    // Method may not be inlined
	;

	private final int value;
	private final MethodImplAttributes masked;

	MethodImplAttributes( int value )
	{
		this.value = value;
		masked = null;
	}

	MethodImplAttributes( MethodImplAttributes masked, int value )
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
