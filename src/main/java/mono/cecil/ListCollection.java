package mono.cecil;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings( "ALL" )
public abstract class ListCollection<T> extends ArrayList<T>
{
	protected ListCollection()
	{
	}

	protected ListCollection( int initialCapacity )
	{
		super( initialCapacity );
	}

	@Override
	public boolean add( T t )
	{
		onAdd( t, size() );
		return super.add( t );
	}

	@Override
	public void add( int index, T element )
	{
		onInsert( element, index );
		super.add( index, element );
	}

	@Override
	public T set( int index, T element )
	{
		onSet( element, index );
		return super.set( index, element );
	}

	@Override
	public T remove( int index )
	{
		onRemove( get( index ), index );
		return super.remove( index );
	}

	@Override
	public boolean remove( Object o )
	{
		int i = indexOf( o );
		if( i == -1 )
			return false;
		onRemove( get( i ), i );
		return super.remove( o );
	}

	@Override
	public void clear()
	{
		onClear();
		super.clear();
	}

	protected void onAdd( T item, int index )
	{
	}

	protected void onSet( T item, int index )
	{
	}

	protected void onRemove( T item, int index )
	{
	}

	protected void onInsert( T item, int index )
	{
	}

	protected void onClear()
	{
	}

	@Override
	public boolean addAll( Collection<? extends T> c )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll( int index, Collection<? extends T> c )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void removeRange( int fromIndex, int toIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll( Collection<?> c )
	{
		throw new UnsupportedOperationException();
	}
}
