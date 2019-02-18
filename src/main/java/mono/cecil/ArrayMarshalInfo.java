package mono.cecil;


public class ArrayMarshalInfo extends MarshalInfo
{
	public ArrayMarshalInfo()
	{
		super( NativeType.Array );
	}

	private NativeType elementType = NativeType.None;
	private int sizeParameterIndex = -1;
	private int size = -1;
	private int sizeParameterMultiplier = -1;

	public NativeType getElementType()
	{
		return elementType;
	}

	public void setElementType( NativeType elementType )
	{
		this.elementType = elementType;
	}

	public int getSizeParameterIndex()
	{
		return sizeParameterIndex;
	}

	public void setSizeParameterIndex( int sizeParameterIndex )
	{
		this.sizeParameterIndex = sizeParameterIndex;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize( int size )
	{
		this.size = size;
	}

	public int getSizeParameterMultiplier()
	{
		return sizeParameterMultiplier;
	}

	public void setSizeParameterMultiplier( int sizeParameterMultiplier )
	{
		this.sizeParameterMultiplier = sizeParameterMultiplier;
	}
}
