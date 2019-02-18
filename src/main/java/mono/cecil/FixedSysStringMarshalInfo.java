package mono.cecil;

public class FixedSysStringMarshalInfo extends MarshalInfo
{
	public FixedSysStringMarshalInfo()
	{
		super( NativeType.FixedSysString );
	}

	private int size = -1;

	public int getSize()
	{
		return size;
	}

	public void setSize( int size )
	{
		this.size = size;
	}
}
