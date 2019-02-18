package mono.cecil;

import mono.cecil.metadata.MetadataToken;

import java.util.Collection;

@SuppressWarnings( {"unused", "NestedAssignment", "ReturnOfCollectionOrArrayField"} )
public class ParameterDefinition extends ParameterReference implements ICustomAttributeProvider, IConstantProvider, IMarshalInfoProvider
{
	public ParameterDefinition( TypeReference parameterType, IMethodSignature method )
	{
		this( null, ParameterAttributes.None.getMask(), parameterType );
		this.method = method;
	}

	public ParameterDefinition( TypeReference parameterType )
	{
		this( null, ParameterAttributes.None.getMask(), parameterType );
	}

	public ParameterDefinition( String name, int attributes, TypeReference parameterType )
	{
		super( name, parameterType );
		this.attributes = attributes;
		setMetadataToken( new MetadataToken( TokenType.Param ) );
	}


	private int attributes;
	private IMethodSignature method;

	private Object constant = Utils.NOT_RESOLVED;
	private Collection<CustomAttribute> customAttributes;
	private MarshalInfo marshalInfo;

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
	}

	public IMethodSignature getMethod()
	{
		return method;
	}

	public void setMethod( IMethodSignature method )
	{
		this.method = method;
	}

	public int getSequence()
	{
		if( method == null )
			return -1;

		return Utils.hasImplicitThis( method ) ? getIndex() + 1 : getIndex();
	}

	@Override
	public boolean hasConstant()
	{
		constant = Utils.resolveConstant( this, getParameterType().getModule(), constant );
		//noinspection ObjectEquality
		return constant != Utils.NO_VALUE;
	}

	@Override
	public void setHasConstant( boolean value )
	{
		if( !value ) constant = Utils.NO_VALUE;
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
	public boolean hasCustomAttributes()
	{
		if( customAttributes != null )
			return !customAttributes.isEmpty();

		return Utils.hasCustomAttributes( this, getParameterType().getModule() );
	}

	@Override
	public Collection<CustomAttribute> getCustomAttributes()
	{
		if( customAttributes != null )
			return customAttributes;
		return customAttributes = Utils.getCustomAttributes( this, getParameterType().getModule() );
	}

	@Override
	public boolean hasMarshalInfo()
	{
		return marshalInfo != null || Utils.getHasMarshalInfo( this, getParameterType().getModule() );
	}

	@Override
	public MarshalInfo getMarshalInfo()
	{
		if( marshalInfo != null )
			return marshalInfo;
		return marshalInfo = Utils.getMarshalInfo( this, getParameterType().getModule() );
	}

	@Override
	public void setMarshalInfo( MarshalInfo info )
	{
		marshalInfo = info;
	}

	public boolean isIn()
	{
		return ParameterAttributes.In.isSet( getAttributes() );
	}

	public boolean isOut()
	{
		return ParameterAttributes.Out.isSet( getAttributes() );
	}

	public boolean isLcid()
	{
		return ParameterAttributes.Lcid.isSet( getAttributes() );
	}

	public boolean isReturnValue()
	{
		return ParameterAttributes.Retval.isSet( getAttributes() );
	}

	public boolean isOptional()
	{
		return ParameterAttributes.Optional.isSet( getAttributes() );
	}

	public boolean isHasDefault()
	{
		return ParameterAttributes.HasDefault.isSet( getAttributes() );
	}

	public void setHasDefault( boolean value )
	{
		attributes = ParameterAttributes.HasDefault.set( value, getAttributes() );
	}

	public boolean hasFieldMarshal()
	{
		return ParameterAttributes.HasFieldMarshal.isSet( getAttributes() );
	}

	@Override
	public ParameterDefinition resolve()
	{
		return this;
	}
}
