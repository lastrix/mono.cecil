package mono.cecil.metadata;

import mono.cecil.Utils;
import mono.cecil.pe.ByteBuffer;
import mono.cecil.pe.Section;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( "ALL" )
public class StringHeap extends Heap
{
	protected Map<Integer, String> map = new HashMap<>();

	public StringHeap( Section section, int offset, int size )
	{
		super( section, offset, size );
	}

	public String read( int index )
	{
		if( index == 0 )
			return Utils.EMPTY;

		String value = map.get( index );
		if( value != null )
			return value;

		if( index > getSize() - 1 )
			return Utils.EMPTY;

		value = readStringAt( index );

		map.put( index, value );
		return value;
	}

	protected String readStringAt( int index )
	{
		ByteBuffer buffer = getSection().data();
		int prevPosition = buffer.position();
		try
		{
			buffer.offset( getOffset() + index );

			return buffer.readUTF8String();
		} finally
		{
			buffer.offset( prevPosition );
		}
	}
}
