package mono.cecil;

public abstract class Resource
{
	Resource( String name, int attributes )
	{
		this.name = name;
		this.attributes = attributes;
	}

	private String name;
	private int attributes;

	public boolean isAttribute( ManifestResourceAttributes attribute )
	{
		return attribute.isSet( attributes );
	}

	public void setAttribute( ManifestResourceAttributes attribute, boolean value )
	{
		attributes = attribute.set( value, attributes );
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
	}

	public boolean isPublic()
	{
		return ManifestResourceAttributes.Public.isSet( attributes );
	}

	public void setPublic( boolean value )
	{
		attributes = ManifestResourceAttributes.Public.set( value, attributes );
	}

	public boolean isPrivate()
	{
		return ManifestResourceAttributes.Private.isSet( attributes );
	}

	public void setPrivate( boolean value )
	{
		attributes = ManifestResourceAttributes.Private.set( value, attributes );
	}

	public abstract ResourceType getResourceType();
}
