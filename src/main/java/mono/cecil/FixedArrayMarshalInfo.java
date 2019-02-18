package mono.cecil;

public class FixedArrayMarshalInfo extends MarshalInfo
{
	public FixedArrayMarshalInfo()
	{
		super( NativeType.FixedArray );
		elementType = NativeType.None;
	}

	private NativeType elementType;
	private int size;

	public NativeType getElementType()
	{
		return elementType;
	}

	public void setElementType( NativeType elementType )
	{
		this.elementType = elementType;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize( int size )
	{
		this.size = size;
	}
}
