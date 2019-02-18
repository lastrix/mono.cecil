package mono.cecil;

import mono.cecil.metadata.MetadataToken;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings( {"unused", "ReturnOfCollectionOrArrayField", "NestedAssignment"} )
public class GenericParameter extends TypeReference implements ICustomAttributeProvider
{
	public GenericParameter( IGenericParameterProvider owner )
	{
		this( null, owner );
	}

	public GenericParameter( String name, IGenericParameterProvider owner )
	{
		super( null, name );
		if( owner == null )
			throw new IllegalArgumentException();

		position = -1;
		this.owner = owner;
		parameterType = owner.getGenericParameterType();
		setEtype( convertGenericParameterType( parameterType ) );
		setMetadataToken( new MetadataToken( TokenType.GenericParam ) );
	}

	public GenericParameter( int position, GenericParameterType parameterType, ModuleDefinition module )
	{
		super( null, null );
		if( module == null )
			throw new IllegalArgumentException();
		this.position = position;
		this.parameterType = parameterType;

		setEtype( convertGenericParameterType( parameterType ) );
		setModule( module );
		setMetadataToken( new MetadataToken( TokenType.GenericParam ) );
	}

	private int position;
	private GenericParameterType parameterType;
	private IGenericParameterProvider owner;

	private int attributes;
	private Collection<TypeReference> constraints;
	private Collection<CustomAttribute> customAttributes;

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
	}

	public int getPosition()
	{
		return position;
	}

	public void setPosition( int position )
	{
		this.position = position;
	}

	public GenericParameterType getParameterType()
	{
		return parameterType;
	}

	public void setParameterType( GenericParameterType parameterType )
	{
		this.parameterType = parameterType;
	}

	public IGenericParameterProvider getOwner()
	{
		return owner;
	}

	public void setOwner( IGenericParameterProvider owner )
	{
		this.owner = owner;
	}

	public boolean hasConstraints()
	{
		if( constraints != null )
			return !constraints.isEmpty();

		if( hasImage() )
			return getModule().read( this, MetadataReader :: hasGenericConstraints );

		return false;
	}

	public Collection<TypeReference> getConstraints()
	{
		if( constraints != null )
			return constraints;

		if( hasImage() )
			return constraints = getModule().read( this, MetadataReader :: readGenericConstraints );

		return constraints = Collections.emptyList();
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
	public IMetadataScope getScope()
	{
		if( owner == null )
			return null;

		return owner.getGenericParameterType() == GenericParameterType.Method
				? ( (MemberReference)owner ).getDeclaringType().getScope()
				: ( (TypeReference)owner ).getScope();
	}

	@Override
	public void setScope( IMetadataScope scope )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public TypeReference getDeclaringType()
	{
		if( owner instanceof TypeReference )
			return (TypeReference)owner;
		return null;
	}

	@Override
	public void setDeclaringType( TypeReference declaringType )
	{
		throw new UnsupportedOperationException();
	}

	public MethodReference getDeclaringMethod()
	{
		if( owner instanceof MethodReference )
			return (MethodReference)owner;
		return null;
	}

	@Override
	public ModuleDefinition getModule()
	{
		ModuleDefinition module = super.getModule();
		if( module == null )
			return owner.getModule();
		return module;
	}

	@Override
	public String getName()
	{
		if( !StringUtils.isBlank( super.getName() ) )
			return super.getName();

		setName( ( parameterType == GenericParameterType.Method ? "!!" : "!" ) + position );
		return super.getName();
	}

	@Override
	public String getNamespace()
	{
		return null;
	}

	@Override
	public void setNamespace( String namespace )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getFullName()
	{
		return getName();
	}

	@Override
	public boolean isGenericParameter()
	{
		return true;
	}

	@Override
	public boolean containsGenericParameter()
	{
		return true;
	}

	@Override
	public MetadataType getMetadataType()
	{
		return MetadataType.getByElementType( getEtype() );
	}

	public boolean isNonVariant()
	{
		return GenericParameterAttributes.NonVariant.isSet( attributes );
	}

	public void setNonVariant( boolean value )
	{
		attributes = GenericParameterAttributes.NonVariant.set( value, attributes );
	}

	public boolean isCovariant()
	{
		return GenericParameterAttributes.Covariant.isSet( attributes );
	}

	public void setCovariant( boolean value )
	{
		attributes = GenericParameterAttributes.Covariant.set( value, attributes );
	}

	public boolean isContravariant()
	{
		return GenericParameterAttributes.Contravariant.isSet( attributes );
	}

	public void setContravariant( boolean value )
	{
		attributes = GenericParameterAttributes.Contravariant.set( value, attributes );
	}

	public boolean isReferenceTypeConstraint()
	{
		return GenericParameterAttributes.ReferenceTypeConstraint.isSet( attributes );
	}

	public void setReferenceTypeConstraint( boolean value )
	{
		attributes = GenericParameterAttributes.ReferenceTypeConstraint.set( value, attributes );
	}

	public boolean isNotNullableValueTypeConstraint()
	{
		return GenericParameterAttributes.NotNullableValueTypeConstraint.isSet( attributes );
	}

	public void setNotNullableValueTypeConstraint( boolean value )
	{
		attributes = GenericParameterAttributes.NotNullableValueTypeConstraint.set( value, attributes );
	}

	public boolean isDefaultConstructorConstraint()
	{
		return GenericParameterAttributes.DefaultConstructorConstraint.isSet( attributes );
	}

	public void setDefaultConstructorConstraint( boolean value )
	{
		attributes = GenericParameterAttributes.DefaultConstructorConstraint.set( value, attributes );
	}

	private static ElementType convertGenericParameterType( GenericParameterType type )
	{
		switch( type )
		{
			case Type:
				return ElementType.Var;
			case Method:
				return ElementType.MVar;
		}

		throw new IllegalArgumentException();
	}

	@Override
	public TypeDefinition resolve()
	{
		return null;
	}
}
