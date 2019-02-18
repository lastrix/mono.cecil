package mono.cecil;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings( {"ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter", "NestedAssignment"} )
public class CustomAttribute implements ICustomAttribute
{
	private static final Logger LOGGER = LoggerFactory.getLogger( CustomAttribute.class );

	public CustomAttribute( int signature, MethodReference constructor )
	{
		this.signature = signature;
		this.constructor = constructor;
		resolved = false;
	}

	public CustomAttribute( MethodReference constructor )
	{
		this.constructor = constructor;
		resolved = true;
	}

	public CustomAttribute( MethodReference constructor, byte[] blob )
	{
		this.constructor = constructor;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.blob = blob;
		resolved = false;
	}

	private int signature;
	private boolean resolved;
	private MethodReference constructor;
	private byte[] blob;
	private List<CustomAttributeArgument> arguments;
	private Collection<CustomAttributeNamedArgument> fields;
	private Collection<CustomAttributeNamedArgument> properties;

	public int getSignature()
	{
		return signature;
	}

	public MethodReference getConstructor()
	{
		return constructor;
	}

	public void setConstructor( MethodReference constructor )
	{
		this.constructor = constructor;
	}

	@Override
	public TypeReference getAttributeType()
	{
		return constructor.getDeclaringType();
	}

	public boolean isResolved()
	{
		resolve();
		return resolved;
	}

	public void setResolved( boolean resolved )
	{
		this.resolved = resolved;
	}

	@SuppressWarnings( "unused" )
	public boolean hasConstructorArguments()
	{
		resolve();
		return arguments != null && !arguments.isEmpty();
	}

	public List<CustomAttributeArgument> getConstructorArguments()
	{
		if( arguments == null )
			arguments = new ArrayList<>();

		resolve();
		return arguments;
	}

	public void setArguments( List<CustomAttributeArgument> arguments )
	{
		this.arguments = arguments;
	}

	public void addArgument( CustomAttributeArgument argument )
	{
		arguments.add( argument );
	}

	@Override
	public boolean hasFields()
	{
		resolve();
		return fields != null && !fields.isEmpty();
	}

	@Override
	public boolean hasProperties()
	{
		resolve();
		return properties != null && !properties.isEmpty();
	}

	@Override
	public Collection<CustomAttributeNamedArgument> getProperties()
	{
		if( properties == null )
			properties = new ArrayList<>();
		resolve();
		return properties;
	}

	@Override
	public Collection<CustomAttributeNamedArgument> getFields()
	{
		if( fields == null )
			fields = new ArrayList<>();

		resolve();
		return fields;
	}

	boolean hasImage()
	{
		return constructor != null && constructor.hasImage();
	}

	public ModuleDefinition getModule()
	{
		return constructor.getModule();
	}

	public byte[] getBlob()
	{
		if( blob != null )
			return blob;

		if( !hasImage() )
			throw new UnsupportedOperationException();

		return blob = getModule().read( this, ( reader, item ) -> reader.readCustomAttributeBlob( item.getSignature() ) );
	}

	public void resolve()
	{
		if( resolved || !hasImage() )
			return;

		getModule().read( this, this :: resolveImpl );
	}

	private CustomAttribute resolveImpl( MetadataReader reader, CustomAttribute item )
	{
		try
		{
			item.setResolved( true );
			reader.readCustomAttributeSignature( item );
		} catch( Exception e )
		{
			LOGGER.debug( "Unable to resolve custom attribute: {}", e.getMessage() );
			if( arguments != null )
				arguments.clear();
			if( fields != null )
				fields.clear();
			if( properties != null )
				properties.clear();

			resolved = false;
		}
		return item;
	}

	@Override
	public String toString()
	{
		if( !resolved )
			return "[UnresolvedAttribute()]";

		StringBuilder sb = new StringBuilder();
		sb.append( '[' ).append( getAttributeType().getFullName() ).append( '(' );
		boolean first = true;
		for( CustomAttributeArgument argument : getConstructorArguments() )
		{
			if( first )
				first = false;
			else sb.append( ',' );
			sb.append( argument );
		}
		sb.append( ")]" );
		return sb.toString();
	}
}
