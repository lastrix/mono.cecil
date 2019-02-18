package mono.cecil;

import mono.cecil.metadata.MetadataToken;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings( {"WeakerAccess", "unused", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter", "ClassReferencesSubclass"} )
public class TypeReference extends MemberReference implements IGenericParameterProvider, IGenericContext
{
	public TypeReference( String namespace, String name )
	{
		super( name );
		this.namespace = namespace;
		setMetadataToken( new MetadataToken( TokenType.TypeRef, 0 ) );
	}

	public TypeReference( String namespace, String name, ModuleDefinition module, @Nullable IMetadataScope scope )
	{
		this( namespace, name );
		this.module = module;
		this.scope = scope;
	}

	public TypeReference( String namespace, String name, ModuleDefinition module, IMetadataScope scope, boolean valueType )
	{
		this( namespace, name, module, scope );
		this.valueType = valueType;
	}

	private String namespace;
	private boolean valueType;
	private IMetadataScope scope;
	private ModuleDefinition module;

	private ElementType etype = ElementType.None;

	private String fullName;

	protected List<GenericParameter> genericParameters;

	public ElementType getEtype()
	{
		return etype;
	}

	public void setEtype( ElementType etype )
	{
		this.etype = etype;
	}

	@Override
	public void setName( String name )
	{
		super.setName( name );
		fullName = null;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public void setNamespace( String namespace )
	{
		this.namespace = namespace;
		fullName = null;
	}

	public boolean isValueType()
	{
		return valueType;
	}

	public void setValueType( boolean valueType )
	{
		this.valueType = valueType;
	}

	@Override
	public ModuleDefinition getModule()
	{
		return module;
	}

	public void setModule( ModuleDefinition module )
	{
		this.module = module;
	}

	@Override
	public IGenericParameterProvider getGenericParameterProviderMethod()
	{
		return null;
	}

	@Override
	public IGenericParameterProvider getGenericParameterProviderType()
	{
		return this;
	}

	@Override
	public GenericParameterType getGenericParameterType()
	{
		return GenericParameterType.Type;
	}

	@Override
	public boolean hasGenericParameters()
	{
		return genericParameters != null && !genericParameters.isEmpty();
	}

	@Override
	public Collection<GenericParameter> getGenericParameters()
	{
		if( genericParameters == null )
			genericParameters = new GenericParameterCollection( this );
		return genericParameters;
	}

	protected void setGenericParameters( List<GenericParameter> genericParameters )
	{
		this.genericParameters = genericParameters;
	}

	@Override
	public GenericParameter getGenericParameter( int index )
	{
		return genericParameters.get( index );
	}

	public IMetadataScope getScope()
	{
		if( getDeclaringType() != null )
			return getDeclaringType().getScope();
		return scope;
	}

	public void setScope( IMetadataScope scope )
	{
		if( getDeclaringType() != null )
			getDeclaringType().setScope( scope );
		else
			this.scope = scope;
	}

	public boolean isNested()
	{
		return getDeclaringType() != null;
	}

	@Override
	public void setDeclaringType( TypeReference declaringType )
	{
		super.setDeclaringType( declaringType );
		fullName = null;
	}

	@Override
	public String getFullName()
	{
		if( fullName != null )
			return fullName;

		fullName = getTypeFullName();

		if( isNested() )
			//noinspection HardcodedFileSeparator
			fullName = getDeclaringType().getFullName() + '/' + fullName;

		return fullName;
	}

	public String getTypeFullName()
	{
		if( namespace == null || namespace.isEmpty() )
			return getName();
		return namespace + '.' + getName();
	}

	public boolean isByReference()
	{
		return false;
	}

	public boolean isPointer()
	{
		return false;
	}

	public boolean isSentinel()
	{
		return false;
	}

	public boolean isArray()
	{
		return false;
	}

	public boolean isGenericParameter()
	{
		return false;
	}

	public boolean isGenericInstance()
	{
		return false;
	}

	public boolean isRequiredModifier()
	{
		return false;
	}

	public boolean isOptionalModifier()
	{
		return false;
	}

	public boolean isPinned()
	{
		return false;
	}

	public boolean isFunctionPointer()
	{
		return false;
	}

	public boolean isPrimitive()
	{
		return etype.isPrimitive();
	}

	public MetadataType getMetadataType()
	{
		switch( etype )
		{
			case None:
				return isValueType() ? MetadataType.ValueType : MetadataType.Class;

			default:
				return MetadataType.getByElementType( etype );
		}
	}

	public TypeReference getElementsType()
	{
		return this;
	}

	public TypeDefinition resolve()
	{
		if( getModule() == null )
			throw new UnsupportedOperationException( "Module is not set." );

		return getModule().resolve( this );
	}

	public boolean isTypeOf( String namespace, String name )
	{
		return Objects.equals( this.namespace, namespace ) && Objects.equals( getName(), name );
	}

	public boolean isTypeSpecification()
	{
		switch( etype )
		{
			case Array:
			case ByRef:
			case CModOpt:
			case CModReqD:
			case FnPtr:
			case GenericInst:
			case MVar:
			case Pinned:
			case Ptr:
			case SzArray:
			case Sentinel:
			case Var:
				return true;

			default:
				return false;
		}
	}

	public TypeDefinition checkedResolve()
	{
		TypeDefinition definition = resolve();
		if( definition == null )
			//log.error( "Unable to resolve type: " + getFullName() );
			throw new IllegalStateException( "Type not resolved: " + getFullName() + " from scope: " + getScope() );

		return definition;
	}

	public static void checkType( TypeReference reference )
	{
		if( reference == null )
			throw new IllegalArgumentException();
	}
}
