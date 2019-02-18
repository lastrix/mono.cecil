package mono.cecil;

@SuppressWarnings( "ALL" )
public class GenericParameterCollection extends ListCollection<GenericParameter>
{
	public GenericParameterCollection( IGenericParameterProvider owner )
	{
		this.owner = owner;
	}

	public GenericParameterCollection( int initialCapacity, IGenericParameterProvider owner )
	{
		super( initialCapacity );
		this.owner = owner;
	}

	private final IGenericParameterProvider owner;

	@Override
	protected void onAdd( GenericParameter item, int index )
	{
		updateGenericParameter( item, index );
	}

	@Override
	protected void onSet( GenericParameter item, int index )
	{
		updateGenericParameter( item, index );
	}

	@Override
	protected void onRemove( GenericParameter item, int index )
	{
		item.setOwner( null );
		item.setPosition( -1 );
		item.setParameterType( GenericParameterType.Type );

		for( int i = index; i < size(); i++ )
			get( i ).setPosition( i - 1 );
	}

	@Override
	protected void onInsert( GenericParameter item, int index )
	{
		updateGenericParameter( item, index );
		for( int i = index; i < size(); i++ )
			get( i ).setPosition( i + 1 );
	}

	private void updateGenericParameter( GenericParameter item, int index )
	{
		item.setOwner( owner );
		item.setPosition( index );
		item.setParameterType( owner.getGenericParameterType() );
	}
}
