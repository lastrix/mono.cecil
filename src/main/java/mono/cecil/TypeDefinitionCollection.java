package mono.cecil;

import mono.cecil.metadata.rows.Row2;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( "ALL" )
public class TypeDefinitionCollection extends ListCollection<TypeDefinition>
{
	public TypeDefinitionCollection( ModuleDefinition container )
	{
		this.container = container;
	}

	public TypeDefinitionCollection( int initialCapacity, ModuleDefinition container )
	{
		super( initialCapacity );
		this.container = container;
	}

	private final ModuleDefinition container;
	private final Map<Row2<String, String>, TypeDefinition> nameCache = new HashMap<>();

	@Override
	protected void onAdd( TypeDefinition item, int index )
	{
		attach( item );
	}

	@Override
	protected void onSet( TypeDefinition item, int index )
	{
		attach( item );
	}

	@Override
	protected void onRemove( TypeDefinition item, int index )
	{
		detach( item );
	}

	@Override
	protected void onInsert( TypeDefinition item, int index )
	{
		attach( item );
	}

	@Override
	protected void onClear()
	{
		for( TypeDefinition typeDefinition : this )
			detach( typeDefinition );
	}

	private void attach( TypeDefinition type )
	{
		//noinspection ObjectEquality
		if( type.getModule() != null && type.getModule() != container )
			throw new IllegalArgumentException();

		type.setModule( container );
		type.setScope( container );
		nameCache.put( new Row2<>( type.getNamespace(), type.getName() ), type );
	}

	private void detach( TypeDefinition type )
	{
		type.setModule( null );
		type.setScope( null );
		nameCache.remove( new Row2<>( type.getNamespace(), type.getName() ) );
	}

	public TypeDefinition getType( String fullName )
	{
		Row2<String, String> row = TypeParser.splitFullName( fullName );
		return getType( row.getCol1(), row.getCol2() );
	}

	public TypeDefinition getType( String namespace, String name )
	{
		return nameCache.get( new Row2<>( namespace, name ) );
	}
}
