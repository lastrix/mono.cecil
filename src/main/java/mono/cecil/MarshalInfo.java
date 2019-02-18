package mono.cecil;

@SuppressWarnings( "unused" )
public class MarshalInfo
{
	public MarshalInfo( NativeType nativeType )
	{
		this.nativeType = nativeType;
	}

	private NativeType nativeType;

	public NativeType getNativeType()
	{
		return nativeType;
	}

	public void setNativeType( NativeType nativeType )
	{
		this.nativeType = nativeType;
	}
}
