package mono.cecil;

import mono.cecil.metadata.MetadataToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings( {"unused", "WeakerAccess", "NestedAssignment", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"} )
public class PropertyDefinition extends PropertyReference implements IMemberDefinition, IConstantProvider
{
	public PropertyDefinition( String name, int attributes, TypeReference propertyType )
	{
		super( name, propertyType );
		this.attributes = attributes;
		setMetadataToken( new MetadataToken( TokenType.Property ) );
	}

	private Boolean hasThis;
	private int attributes;
	private Collection<CustomAttribute> customAttributes;

	private MethodDefinition getMethod;
	private MethodDefinition setMethod;
	private Collection<MethodDefinition> otherMethods;

	private Object constant = Utils.NOT_RESOLVED;

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
	}

	public Boolean getHasThis()
	{
		if( hasThis != null )
			return hasThis;

		if( getMethod != null )
			return getMethod.getHasThis();

		//noinspection SimplifiableIfStatement
		if( setMethod != null )
			return setMethod.getHasThis();

		return false;
	}

	public void setHasThis( Boolean hasThis )
	{
		this.hasThis = hasThis;
	}

	public boolean hasOtherMethods()
	{
		if( otherMethods == null )
			initializeMethods();
		return !otherMethods.isEmpty();
	}

	public Collection<MethodDefinition> getOtherMethods()
	{
		if( otherMethods != null )
			return otherMethods;

		initializeMethods();

		if( otherMethods != null )
			return otherMethods;

		return otherMethods = new ArrayList<>();
	}

	public void setOtherMethods( Collection<MethodDefinition> otherMethods )
	{
		this.otherMethods = otherMethods;
	}

	public void addOtherMethod( MethodDefinition method )
	{
		otherMethods.add( method );
	}

	@Override
	public boolean hasCustomAttributes()
	{
		if( customAttributes != null )
			return !customAttributes.isEmpty();

		return Utils.hasCustomAttributes( this, getModule() );
	}

	@Override
	public Collection<CustomAttribute> getCustomAttributes()
	{
		if( customAttributes != null )
			return customAttributes;
		return customAttributes = Utils.getCustomAttributes( this, getModule() );
	}

	public MethodDefinition getGetMethod()
	{
		if( getMethod != null )
			return getMethod;

		initializeMethods();
		return getMethod;
	}

	public void setGetMethod( MethodDefinition getMethod )
	{
		this.getMethod = getMethod;
	}

	public MethodDefinition getSetMethod()
	{
		if( setMethod != null )
			return setMethod;

		initializeMethods();
		return setMethod;
	}

	public void setSetMethod( MethodDefinition setMethod )
	{
		this.setMethod = setMethod;
	}

	public boolean hasParameters()
	{
		initializeMethods();

		if( getMethod != null )
			return getMethod.hasParameters();

		//noinspection SimplifiableIfStatement
		if( setMethod != null )
			return setMethod.hasParameters() && setMethod.getParameters().size() > 1;

		return false;
	}

	@Override
	public Collection<ParameterDefinition> getParameters()
	{
		initializeMethods();

		if( getMethod != null )
			return mirrorParameters( getMethod, 0 );

		if( setMethod != null )
			return mirrorParameters( setMethod, 1 );

		return Collections.emptyList();
	}

	@Override
	public boolean hasConstant()
	{
		constant = Utils.resolveConstant( this, getModule(), constant );
		//noinspection ObjectEquality
		return constant != Utils.NO_VALUE;
	}

	@Override
	public void setHasConstant( boolean value )
	{
		if( !value )
			constant = Utils.NO_VALUE;
	}

	@Override
	public Object getConstant()
	{
		return hasConstant() ? constant : null;
	}

	@Override
	public void setConstant( Object value )
	{
		constant = value;
	}

	@Override
	public boolean isSpecialName()
	{
		return PropertyAttributes.SpecialName.isSet( getAttributes() );
	}

	@Override
	public void setSpecialName( boolean value )
	{
		attributes = PropertyAttributes.SpecialName.set( value, getAttributes() );
	}

	@Override
	public boolean isRuntimeSpecialName()
	{
		return PropertyAttributes.RTSpecialName.isSet( getAttributes() );
	}

	@Override
	public void setRuntimeSpecialName( boolean value )
	{
		attributes = PropertyAttributes.RTSpecialName.set( value, getAttributes() );
	}

	public boolean isHasDefault()
	{
		return PropertyAttributes.HasDefault.isSet( getAttributes() );
	}

	public void setHasDefault( boolean value )
	{
		attributes = PropertyAttributes.HasDefault.set( value, getAttributes() );
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
	public String getFullName()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( getPropertyType() );
		sb.append( ' ' );
		sb.append( getMemberFullName() );
		sb.append( '(' );
		if( hasParameters() )
		{
			boolean first = true;
			for( ParameterDefinition parameter : getParameters() )
			{
				if( first )
					first = false;
				else
					sb.append( ", " );

				sb.append( parameter.getParameterType().getFullName() );
			}
		}
		sb.append( ')' );
		return sb.toString();
	}

	@Override
	public PropertyDefinition resolve()
	{
		return this;
	}

	private static Collection<ParameterDefinition> mirrorParameters( IMethodSignature method, int bound )
	{
		if( !method.hasParameters() )
			return Collections.emptyList();

		Collection<ParameterDefinition> list = new ArrayList<>();
		Collection<ParameterDefinition> parameters = method.getParameters();
		int end = parameters.size() - bound;
		int index = 0;
		for( ParameterDefinition parameter : parameters )
		{
			if( index == end )
				break;
			list.add( parameter );
			index++;
		}
		return list;
	}

	private void initializeMethods()
	{
		if( getModule() == null )
			return;

		if( getMethod != null || setMethod != null )
			return;

		if( !getModule().hasImage() || initializing )
			return;

		initializing = true;
		getModule().read( this, (Callback<Object, PropertyDefinition>)MetadataReader :: readMethods );
		initializing = false;
	}

	private boolean initializing;

}
