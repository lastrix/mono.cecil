package mono.cecil;

public class LinkedResource extends Resource
{
	public LinkedResource( String name, int attributes )
	{
		super( name, attributes );
	}

	public LinkedResource( String name, int attributes, String file )
	{
		super( name, attributes );
		this.file = file;
	}

	private byte[] hash;
	private String file;

	public byte[] getHash()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return hash;
	}

	public String getFile()
	{
		return file;
	}

	public void setFile( String file )
	{
		this.file = file;
	}

	public void setHash( byte[] hash )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.hash = hash;
	}

	@Override
	public ResourceType getResourceType()
	{
		return ResourceType.Linked;
	}
}
