package mono.cecil;


public enum MetadataType
{
	Void( ElementType.Void ),
	Boolean( ElementType.Boolean ),
	Char( ElementType.Char ),
	SByte( ElementType.I1 ),
	Byte( ElementType.U1 ),
	Int16( ElementType.I2 ),
	UInt16( ElementType.U2 ),
	Int32( ElementType.I4 ),
	UInt32( ElementType.U4 ),
	Int64( ElementType.I8 ),
	UInt64( ElementType.U8 ),
	Single( ElementType.R4 ),
	Double( ElementType.R8 ),
	String( ElementType.String ),
	Pointer( ElementType.Ptr ),
	ByReference( ElementType.ByRef ),
	ValueType( ElementType.ValueType ),
	Class( ElementType.Class ),
	Var( ElementType.Var ),
	Array( ElementType.Array ),
	GenericInstance( ElementType.GenericInst ),
	TypedByReference( ElementType.TypedByRef ),
	IntPtr( ElementType.I ),
	UIntPtr( ElementType.U ),
	FunctionPointer( ElementType.FnPtr ),
	Object( ElementType.Object ),
	MVar( ElementType.MVar ),
	RequiredModifier( ElementType.CModReqD ),
	OptionalModifier( ElementType.CModOpt ),
	Sentinel( ElementType.Sentinel ),
	Pinned( ElementType.Pinned );

	private final ElementType type;

	MetadataType( ElementType type )
	{
		this.type = type;
	}

	public ElementType getType()
	{
		return type;
	}

	public int getCode()
	{
		return type.getCode();
	}

	public static MetadataType getByElementType( ElementType type )
	{
		for( MetadataType metadataType : values() )
		{
			if( metadataType.getType() == type )
				return metadataType;
		}

		throw new IllegalArgumentException( "Unknown type: " + type );
	}
}
