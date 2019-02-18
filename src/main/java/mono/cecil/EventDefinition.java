package mono.cecil;

import mono.cecil.metadata.MetadataToken;

import java.util.Collection;

@SuppressWarnings( {"unused", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"} )
public class EventDefinition extends EventReference implements IMemberDefinition
{
	public EventDefinition( String name, TypeReference eventType, int attributes )
	{
		super( name, eventType );
		this.attributes = attributes;
		setMetadataToken( new MetadataToken( TokenType.Event ) );
	}

	private int attributes;
	private Collection<CustomAttribute> customAttributes;
	private MethodDefinition addMethod;
	private MethodDefinition invokeMethod;
	private MethodDefinition removeMethod;
	private Collection<MethodDefinition> otherMethods;

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
	}

	public MethodDefinition getAddMethod()
	{
		if( addMethod != null )
			return addMethod;
		initializeMethods();
		return addMethod;
	}

	public void setAddMethod( MethodDefinition addMethod )
	{
		this.addMethod = addMethod;
	}

	public MethodDefinition getInvokeMethod()
	{
		if( invokeMethod != null )
			return invokeMethod;
		initializeMethods();
		return invokeMethod;
	}

	public void setInvokeMethod( MethodDefinition invokeMethod )
	{
		this.invokeMethod = invokeMethod;
	}

	public MethodDefinition getRemoveMethod()
	{
		if( removeMethod != null )
			return removeMethod;
		initializeMethods();
		return removeMethod;
	}

	public void setRemoveMethod( MethodDefinition removeMethod )
	{
		this.removeMethod = removeMethod;
	}

	public boolean hasOtherMethods()
	{
		if( otherMethods != null )
			return !otherMethods.isEmpty();

		initializeMethods();
		return !otherMethods.isEmpty();
	}

	public void addOtherMethod( MethodDefinition method )
	{
		otherMethods.add( method );
	}

	public Collection<MethodDefinition> getOtherMethods()
	{
		if( otherMethods != null )
			return otherMethods;

		initializeMethods();
		return otherMethods;
	}

	public void setOtherMethods( Collection<MethodDefinition> otherMethods )
	{
		this.otherMethods = otherMethods;
	}

	@Override
	public boolean hasCustomAttributes()
	{
		return customAttributes != null && !customAttributes.isEmpty();
	}

	@Override
	public Collection<CustomAttribute> getCustomAttributes()
	{
		if( customAttributes != null )
			//noinspection ReturnOfCollectionOrArrayField
			return customAttributes;
		return Utils.getCustomAttributes( this, getModule() );
	}

	@Override
	public boolean isSpecialName()
	{
		return EventAttributes.SpecialName.isSet( getAttributes() );
	}

	@Override
	public void setSpecialName( boolean value )
	{
		attributes = EventAttributes.SpecialName.set( value, getAttributes() );
	}

	@Override
	public boolean isRuntimeSpecialName()
	{
		return EventAttributes.RTSpecialName.isSet( getAttributes() );
	}

	@Override
	public void setRuntimeSpecialName( boolean value )
	{
		attributes = EventAttributes.RTSpecialName.set( value, getAttributes() );
	}

	@Override
	public TypeDefinition getDeclaringType()
	{
		return (TypeDefinition)super.getDeclaringType();
	}

	@Override
	public void setDeclaringType( TypeDefinition type )
	{
		super.setDeclaringType( type );
	}

	@Override
	public boolean isDefinition()
	{
		return true;
	}

	@Override
	public EventDefinition resolve()
	{
		return this;
	}

	private void initializeMethods()
	{
		ModuleDefinition module = getModule();
		if( module == null )
			return;

		if( addMethod != null
				|| invokeMethod != null
				|| removeMethod != null )
			return;

		if( !module.hasImage() )
			return;

		module.read( this, MetadataReader :: readMethods );
	}
}
