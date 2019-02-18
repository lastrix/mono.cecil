package mono.cecil.metadata;

import mono.cecil.Utils;
import mono.cecil.pe.ByteBuffer;
import mono.cecil.pe.Section;

public class UserStringHeap extends StringHeap
{
	public UserStringHeap( Section section, int offset, int size )
	{
		super( section, offset, size );
	}

	@Override
	protected String readStringAt( int index )
	{
		ByteBuffer buffer = getSection().data();
		int prevPosition = buffer.position();
		try
		{
			// TODO: возможно тут косяк с переводов мультибайта в строку
			int position = getOffset() + index;
			buffer.offset( position );
			int length = buffer.readCompressedUInt32();
			int[] chars = new int[length / 2];
			for( int i = position, j = 0; i < position + length; i += 2 )
				//noinspection NumericCastThatLosesPrecision,ValueOfIncrementOrDecrementUsed,AssignmentToForLoopParameter
				chars[j++] = ( buffer.readByte() & Utils.BYTE_MASK ) | ( buffer.readByte() & Utils.BYTE_MASK ) << 8;

			return new String( chars, 0, chars.length );
		} finally
		{
			buffer.offset( prevPosition );
		}
	}
}
