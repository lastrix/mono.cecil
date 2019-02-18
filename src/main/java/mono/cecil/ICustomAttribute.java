package mono.cecil;

import java.util.Collection;

@SuppressWarnings( "unused" )
public interface ICustomAttribute
{
	TypeReference getAttributeType();

	boolean hasFields();

	boolean hasProperties();

	Collection<CustomAttributeNamedArgument> getFields();

	Collection<CustomAttributeNamedArgument> getProperties();
}
