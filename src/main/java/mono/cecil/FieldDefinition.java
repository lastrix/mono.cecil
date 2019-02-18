package mono.cecil;

import java.util.Collection;

@SuppressWarnings( {"unused", "NestedAssignment", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"} )
public class FieldDefinition extends FieldReference implements IMemberDefinition, IConstantProvider, IMarshalInfoProvider
{
	public FieldDefinition( String name, int attributes, TypeReference fieldType )
	{
		super( name, fieldType );
		this.attributes = attributes;
	}

	private int attributes;
	private Collection<CustomAttribute> customAttributes;
	private int offset = Utils.NOT_RESOLVED_MARK;

	private int rva = Utils.NOT_RESOLVED_MARK;
	private byte[] initialValue;
	private Object constant = Utils.NOT_RESOLVED;
	private MarshalInfo marshalInfo;

	public boolean hasFieldLayout()
	{
		if( offset >= 0 )
			return true;

		resolveLayout();

		return offset >= 0;
	}

	public int getOffset()
	{
		if( offset >= 0 )
			return offset;

		resolveLayout();
		return offset >= 0 ? offset : -1;
	}

	public void setOffset( int offset )
	{
		this.offset = offset;
	}

	public int getRva()
	{
		if( rva > 0 )
			return rva;

		resolveRva();
		return rva > 0 ? rva : 0;
	}

	public void setRva( int rva )
	{
		this.rva = rva;
	}

	public byte[] getInitialValue()
	{
		if( initialValue != null )
			return initialValue;

		resolveRva();

		if( initialValue == null )
			initialValue = Utils.EMPTY_BYTE_ARRAY;

		return initialValue;
	}

	public void setInitialValue( byte[] initialValue )
	{
		this.initialValue = initialValue;
	}

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
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

	@Override
	public boolean hasMarshalInfo()
	{
		return marshalInfo != null || Utils.getHasMarshalInfo( this, getModule() );
	}

	@Override
	public MarshalInfo getMarshalInfo()
	{
		if( marshalInfo != null )
			return marshalInfo;

		return marshalInfo = Utils.getMarshalInfo( this, getModule() );
	}

	@Override
	public void setMarshalInfo( MarshalInfo info )
	{
		marshalInfo = info;
	}

	/////
	public boolean isCompilerControlled()
	{
		return FieldAttributes.CompilerControlled.isSet( attributes );
	}

	public void setCompilerController( boolean value )
	{
		attributes = FieldAttributes.CompilerControlled.set( value, attributes );
	}

	public boolean isPrivate()
	{
		return FieldAttributes.Private.isSet( attributes );
	}

	public void setPrivate( boolean value )
	{
		attributes = FieldAttributes.Private.set( value, attributes );
	}

	public boolean isFamilyAndAssembly()
	{
		return FieldAttributes.FamANDAssem.isSet( attributes );
	}

	public void setFamilyAndAssembly( boolean value )
	{
		attributes = FieldAttributes.FamANDAssem.set( value, attributes );
	}

	public boolean isAssembly()
	{
		return FieldAttributes.Assembly.isSet( attributes );
	}

	public void setAssembly( boolean value )
	{
		attributes = FieldAttributes.Assembly.set( value, attributes );
	}

	public boolean isFamily()
	{
		return FieldAttributes.Family.isSet( attributes );
	}

	public void setFamily( boolean value )
	{
		attributes = FieldAttributes.Family.set( value, attributes );
	}

	public boolean isFamilyOrAssembly()
	{
		return FieldAttributes.FamORAssem.isSet( attributes );
	}

	public void setFamilyOrAssembly( boolean value )
	{
		attributes = FieldAttributes.FamORAssem.set( value, attributes );
	}

	public boolean isPublic()
	{
		return FieldAttributes.Public.isSet( attributes );
	}

	public void setPublic( boolean value )
	{
		attributes = FieldAttributes.Public.set( value, attributes );
	}

	public boolean isStatic()
	{
		return FieldAttributes.Static.isSet( attributes );
	}

	public void setStatic( boolean value )
	{
		attributes = FieldAttributes.Static.set( value, attributes );
	}

	public boolean isInitOnly()
	{
		return FieldAttributes.InitOnly.isSet( attributes );
	}

	public void setInitOnly( boolean value )
	{
		attributes = FieldAttributes.InitOnly.set( value, attributes );
	}

	public boolean isLiteral()
	{
		return FieldAttributes.Literal.isSet( attributes );
	}

	public void setLiteral( boolean value )
	{
		attributes = FieldAttributes.Literal.set( value, attributes );
	}

	public boolean isNotSerialized()
	{
		return FieldAttributes.NotSerialized.isSet( attributes );
	}

	public void setNotSerialized( boolean value )
	{
		attributes = FieldAttributes.NotSerialized.set( value, attributes );
	}

	@Override
	public boolean isSpecialName()
	{
		return FieldAttributes.SpecialName.isSet( attributes );
	}

	@Override
	public void setSpecialName( boolean value )
	{
		attributes = FieldAttributes.SpecialName.set( value, attributes );
	}

	public boolean isPInvokeImpl()
	{
		return FieldAttributes.PInvokeImpl.isSet( attributes );
	}

	public void setPInvokeImpl( boolean value )
	{
		attributes = FieldAttributes.PInvokeImpl.set( value, attributes );
	}

	@Override
	public boolean isRuntimeSpecialName()
	{
		return FieldAttributes.RTSpecialName.isSet( attributes );
	}

	@Override
	public void setRuntimeSpecialName( boolean value )
	{
		attributes = FieldAttributes.RTSpecialName.set( value, attributes );
	}

	public boolean isHasDefault()
	{
		return FieldAttributes.HasDefault.isSet( attributes );
	}

	public void setHasDefault( boolean value )
	{
		attributes = FieldAttributes.HasDefault.set( value, attributes );
	}

	/////
	@Override
	public boolean isDefinition()
	{
		return true;
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
	public FieldDefinition resolve()
	{
		return this;
	}

	private void resolveLayout()
	{
		if( offset != Utils.NOT_RESOLVED_MARK )
			return;

		if( !hasImage() )
		{
			offset = Utils.NO_DATA_MARK;
			return;
		}

		offset = getModule().read( this, MetadataReader :: readFieldLayout );
	}

	private void resolveRva()
	{
		if( rva != Utils.NOT_RESOLVED_MARK )
			return;

		if( !hasImage() )
			return;

		rva = getModule().read( this, MetadataReader :: readFieldRVA );
	}
}
