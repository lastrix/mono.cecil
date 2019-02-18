package mono.cecil.metadata;

import mono.cecil.Utils;
import mono.cecil.pe.ByteBuffer;
import mono.cecil.pe.Section;

public class BlobHeap extends Heap
{
	public BlobHeap( Section section, int offset, int size )
	{
		super( section, offset, size );
	}

	public byte[] read( int index )
	{
		if( index == 0 || index >= getSize() )
			return Utils.EMPTY_BYTE_ARRAY;

		ByteBuffer data = getSection().data();
		int position = index + getOffset();
		data.offset( position );
		int length = data.readCompressedUInt32();
		if( length < 0 )
			throw new IllegalArgumentException();
		byte[] result = new byte[length];
		data.read( result, 0, length );
		return result;
	}
}
