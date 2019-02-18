package mono.cecil.pe;

import mono.cecil.Range;

@SuppressWarnings( "ALL" )
public class TextMap
{
	private final Range[] map = new Range[TextSegment.values().length];

	public void addMap( TextSegment segment, int length )
	{
		map[segment.ordinal()] = new Range( getStart( segment ), length );
	}

	public void addMap( TextSegment segment, int length, int align )
	{
		align--;

		addMap( segment, ( length + align ) & ~align );
	}

	public void addMap( TextSegment segment, Range range )
	{
		map[segment.ordinal()] = range;
	}

	public Range getRange( TextSegment segment )
	{
		return map[segment.ordinal()];
	}

	public DataDirectory getDataDirectory( TextSegment segment )
	{
		Range range = map[segment.ordinal()];
		return new DataDirectory( range.getLength() == 0 ? 0 : range.getStart(), range.getLength() );
	}

	public int getRva( TextSegment segment )
	{
		return map[segment.ordinal()].getStart();
	}

	public int getNextRva( TextSegment segment )
	{
		int i = segment.ordinal();
		return map[i].getStart() + map[i].getLength();
	}

	public int getLength( TextSegment segment )
	{
		return map[segment.ordinal()].getLength();
	}

	private int getStart( TextSegment segment )
	{
		int index = segment.ordinal();
		return index == 0 ? IMAGE_WRITER_TEXT_RVA : computeStart( index );
	}

	private int computeStart( int index )
	{
		index--;
		return map[index].getStart() + map[index].getLength();
	}

	public int getLength()
	{
		Range range = map[TextSegment.StartupStub.ordinal()];
		return range.getStart() - IMAGE_WRITER_TEXT_RVA + range.getLength();
	}

	private static final int IMAGE_WRITER_TEXT_RVA = 0x2000;
}
