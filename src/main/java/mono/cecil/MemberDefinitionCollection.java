package mono.cecil;


@SuppressWarnings( "ALL" )
public class MemberDefinitionCollection<T extends IMemberDefinition> extends ListCollection<T>
{
	public MemberDefinitionCollection( TypeDefinition container )
	{
		this.container = container;
	}

	public MemberDefinitionCollection( int initialCapacity, TypeDefinition container )
	{
		super( initialCapacity );
		this.container = container;
	}

	private final TypeDefinition container;

	@Override
	protected void onAdd( T item, int index )
	{
		attach( item );
	}

	@Override
	protected void onSet( T item, int index )
	{
		attach( item );
	}

	@Override
	protected void onRemove( T item, int index )
	{
		detach( item );
	}

	@Override
	protected void onInsert( T item, int index )
	{
		attach( item );
	}

	@Override
	protected void onClear()
	{
		for( T item : this )
			detach( item );
	}

	private void attach( T item )
	{
		//noinspection ObjectEquality
		if( item.getDeclaringType() == container )
			return;

		if( item.getDeclaringType() != null )
			throw new IllegalArgumentException();

		item.setDeclaringType( container );
	}

	private void detach( T item )
	{
		item.setDeclaringType( null );
	}
}
