package mono.cecil;

public class SafeArrayMarshalInfo extends MarshalInfo
{
	public SafeArrayMarshalInfo()
	{
		super( NativeType.SafeArray );
		elementType = VariantType.None;
	}

	private VariantType elementType;

	public VariantType getElementType()
	{
		return elementType;
	}

	public void setElementType( VariantType elementType )
	{
		this.elementType = elementType;
	}
}
