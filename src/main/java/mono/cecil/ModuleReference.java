package mono.cecil;

import mono.cecil.metadata.MetadataToken;

public class ModuleReference implements IMetadataScope
{
	protected ModuleReference()
	{
		metadataToken = new MetadataToken( TokenType.ModuleRef );
	}

	public ModuleReference( String name )
	{
		this();
		this.name = name;
	}

	private String name;
	private MetadataToken metadataToken;

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName( String name )
	{
		this.name = name;
	}

	@Override
	public MetadataToken getMetadataToken()
	{
		return metadataToken;
	}

	@Override
	public void setMetadataToken( MetadataToken token )
	{
		metadataToken = token;
	}

	@Override
	public MetadataScopeType getMetadataScopeType()
	{
		return MetadataScopeType.ModuleReference;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
