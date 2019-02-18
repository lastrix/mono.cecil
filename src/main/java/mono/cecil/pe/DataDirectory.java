package mono.cecil.pe;

@SuppressWarnings( "ALL" )
public class DataDirectory
{
	private final int virtualAddress;
	private final int size;

	public DataDirectory( int virtualAddress, int size )
	{
		this.virtualAddress = virtualAddress;
		this.size = size;
	}

	public boolean isZero()
	{
		return virtualAddress == 0 && size == 0;
	}

	public int getVirtualAddress()
	{
		return virtualAddress;
	}

	public int getSize()
	{
		return size;
	}

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) return true;
		if( !( o instanceof DataDirectory ) ) return false;

		DataDirectory that = (DataDirectory)o;

		if( getVirtualAddress() != that.getVirtualAddress() ) return false;
		if( getSize() != that.getSize() ) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = getVirtualAddress();
		result = 31 * result + getSize();
		return result;
	}

	@Override
	public String toString()
	{
		return "DataDirectory{" +
				"virtualAddress=" + Integer.toHexString( virtualAddress ) +
				", size=" + size +
				'}';
	}
}
