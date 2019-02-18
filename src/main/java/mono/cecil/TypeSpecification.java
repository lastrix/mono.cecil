package mono.cecil;


import mono.cecil.metadata.MetadataToken;

public class TypeSpecification extends TypeReference
{
	public TypeSpecification( TypeReference type )
	{
		super( null, null );
		elementType = type;
		setMetadataToken( new MetadataToken( TokenType.TypeSpec ) );
	}

	private TypeReference elementType;

	public final TypeReference getElementType()
	{
		return elementType;
	}

	public final void setElementType( TypeReference elementType )
	{
		this.elementType = elementType;
	}

	@Override
	public String getName()
	{
		return elementType.getName();
	}

	@Override
	public void setName( String name )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNamespace()
	{
		return elementType.getNamespace();
	}

	@Override
	public void setNamespace( String namespace )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IMetadataScope getScope()
	{
		return elementType.getScope();
	}

	@Override
	public void setScope( IMetadataScope scope )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ModuleDefinition getModule()
	{
		return elementType.getModule();
	}

	@Override
	public String getFullName()
	{
		return elementType.getFullName();
	}

	@Override
	public boolean containsGenericParameter()
	{
		return elementType.containsGenericParameter();
	}

	@Override
	public MetadataType getMetadataType()
	{
		return MetadataType.getByElementType( getEtype() );
	}

	@Override
	public TypeReference getElementsType()
	{
		return elementType.getElementsType();
	}
}
