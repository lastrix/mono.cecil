package mono.cecil;

public class EmbeddedResource extends Resource
{
	public EmbeddedResource( String name, int attributes )
	{
		super( name, attributes );
	}

	public EmbeddedResource( String name, int attributes, int position, MetadataReader reader )
	{
		super( name, attributes );
		this.position = position;
		this.reader = reader;
	}

	private MetadataReader reader;

	private int position;
	private byte[] data;

//	public byte[] getResourceData()
//	{
//		throw new UnsupportedOperationException();
//	}

//	public Object getResourceStream()
//	{
//		throw new UnsupportedOperationException();
//	}

	@Override
	public ResourceType getResourceType()
	{
		return ResourceType.Embedded;
	}
}
