package mono.cecil.metadata;

import mono.cecil.Guid;
import mono.cecil.pe.Section;

@SuppressWarnings( "ALL" )
public class GuidHeap extends Heap
{
	public GuidHeap( Section section, int offset, int size )
	{
		super( section, offset, size );
	}

	public Guid read( int index )
	{
		if( index == 0 )
			return new Guid();

		byte[] buffer = new byte[Guid.SIZE];
		index--;
		getSection().data().read( buffer, 0, Guid.SIZE );
		return new Guid( buffer );
	}
}
