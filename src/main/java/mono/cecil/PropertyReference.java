package mono.cecil;

import java.util.Collection;

public abstract class PropertyReference extends MemberReference
{
	protected PropertyReference( String name, TypeReference propertyType )
	{
		super( name );
		if( propertyType == null )
			throw new IllegalArgumentException();
		this.propertyType = propertyType;
	}

	private TypeReference propertyType;

	public TypeReference getPropertyType()
	{
		return propertyType;
	}

	public void setPropertyType( TypeReference propertyType )
	{
		this.propertyType = propertyType;
	}

	public abstract Collection<ParameterDefinition> getParameters();

	@SuppressWarnings( "ClassReferencesSubclass" )
	public abstract PropertyDefinition resolve();
}
