package mono.cecil;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings( {"ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"} )
public class SecurityAttribute implements ICustomAttribute
{
	public SecurityAttribute( TypeReference attributeType )
	{
		this.attributeType = attributeType;
	}

	private TypeReference attributeType;
	private Collection<CustomAttributeNamedArgument> fields;
	private Collection<CustomAttributeNamedArgument> properties;

	@Override
	public TypeReference getAttributeType()
	{
		return attributeType;
	}

	public void setAttributeType( TypeReference attributeType )
	{
		this.attributeType = attributeType;
	}

	@Override
	public boolean hasFields()
	{
		return fields != null && !fields.isEmpty();
	}

	@Override
	public boolean hasProperties()
	{
		return properties != null && !properties.isEmpty();
	}

	@Override
	public Collection<CustomAttributeNamedArgument> getFields()
	{
		if( fields == null )
			fields = new ArrayList<>();
		return fields;
	}

	@Override
	public Collection<CustomAttributeNamedArgument> getProperties()
	{
		if( properties == null )
			properties = new ArrayList<>();
		return properties;
	}

	public void setProperties( Collection<CustomAttributeNamedArgument> properties )
	{
		this.properties = properties;
	}

	public void addProperty( CustomAttributeNamedArgument property )
	{
		properties.add( property );
	}
}
