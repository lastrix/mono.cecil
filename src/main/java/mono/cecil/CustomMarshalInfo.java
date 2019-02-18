package mono.cecil;

@SuppressWarnings( "unused" )
public class CustomMarshalInfo extends MarshalInfo
{
	public CustomMarshalInfo()
	{
		super( NativeType.CustomMarshaler );
	}

	private Guid guid;
	private String unmanagedType;
	private TypeReference managedType;
	private String cookie;

	public Guid getGuid()
	{
		return guid;
	}

	public void setGuid( Guid guid )
	{
		this.guid = guid;
	}

	public String getUnmanagedType()
	{
		return unmanagedType;
	}

	public void setUnmanagedType( String unmanagedType )
	{
		this.unmanagedType = unmanagedType;
	}

	public TypeReference getManagedType()
	{
		return managedType;
	}

	public void setManagedType( TypeReference managedType )
	{
		this.managedType = managedType;
	}

	public String getCookie()
	{
		return cookie;
	}

	public void setCookie( String cookie )
	{
		this.cookie = cookie;
	}
}
