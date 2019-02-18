package mono.cecil;

@SuppressWarnings( {"CloneableClassWithoutClone", "CloneableClassInSecureContext"} )
public final class ParameterDefinitionCollection extends ListCollection<ParameterDefinition>
{
	public ParameterDefinitionCollection( IMethodSignature method )
	{
		this.method = method;
	}

	public ParameterDefinitionCollection( int initialCapacity, IMethodSignature method )
	{
		super( initialCapacity );
		this.method = method;
	}

	private final IMethodSignature method;

	@Override
	protected void onAdd( ParameterDefinition item, int index )
	{
		item.setMethod( method );
		item.setIndex( index );
	}

	@Override
	protected void onSet( ParameterDefinition item, int index )
	{
		item.setMethod( method );
		item.setIndex( index );
	}

	@Override
	protected void onRemove( ParameterDefinition item, int index )
	{
		item.setMethod( null );
		item.setIndex( -1 );

		for( int i = index + 1; i < size(); i++ )
			get( i ).setIndex( i - 1 );
	}

	@Override
	protected void onInsert( ParameterDefinition item, int index )
	{
		item.setMethod( method );
		item.setIndex( index );

		for( int i = index + 1; i < size(); i++ )
			get( i ).setIndex( i + 1 );
	}
}
