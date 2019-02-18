package mono.cecil.pe;

@SuppressWarnings( "ALL" )
public final class Section
{
	private String name;
	private int virtualAddress;
	private int virtualSize;
	private int sizeOfRawData;
	private int pointerToRawData;
	private ByteBuffer data;

	public Section()
	{
	}

	public Section name( String name )
	{
		this.name = name;
		return this;
	}

	public Section virtualAddress( int virtualAddress )
	{
		this.virtualAddress = virtualAddress;
		return this;
	}

	public Section virtualSize( int virtualSize )
	{
		this.virtualSize = virtualSize;
		return this;
	}

	public Section sizeOfRawData( int sizeOfRawData )
	{
		this.sizeOfRawData = sizeOfRawData;
		return this;
	}

	public Section pointerToRawData( int pointerToRawData )
	{
		this.pointerToRawData = pointerToRawData;
		return this;
	}

	public Section data( ByteBuffer data )
	{
		this.data = data;
		return this;
	}

	public String name()
	{
		return name;
	}

	public int virtualAddress()
	{
		return virtualAddress;
	}

	public int virtualSize()
	{
		return virtualSize;
	}

	public int sizeOfRawData()
	{
		return sizeOfRawData;
	}

	public int pointerToRawData()
	{
		return pointerToRawData;
	}

	public ByteBuffer data()
	{
		return data;
	}

}
