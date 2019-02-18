package mono.cecil;

import mono.cecil.metadata.MetadataToken;
import org.apache.commons.lang3.StringUtils;

public abstract class ParameterReference implements IMetadataTokenProvider
{
	protected ParameterReference( String name, TypeReference parameterType )
	{
		if( parameterType == null )
			throw new IllegalArgumentException();
		this.name = StringUtils.isBlank( name ) ? Utils.EMPTY : name;
		this.parameterType = parameterType;
	}

	private String name;
	private int index = -1;
	private TypeReference parameterType;
	private MetadataToken metadataToken;

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex( int index )
	{
		this.index = index;
	}

	public TypeReference getParameterType()
	{
		return parameterType;
	}

	public void setParameterType( TypeReference parameterType )
	{
		this.parameterType = parameterType;
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
	public String toString()
	{
		return name;
	}

	@SuppressWarnings( "ClassReferencesSubclass" )
	public abstract ParameterDefinition resolve();
}
