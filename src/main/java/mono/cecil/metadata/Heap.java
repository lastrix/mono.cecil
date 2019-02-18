package mono.cecil.metadata;

import mono.cecil.pe.Section;

public abstract class Heap
{
	private int indexSize;

	private final Section section;
	private final int offset;
	private final int size;

	protected Heap( Section section, int offset, int size )
	{
		this.section = section;
		this.offset = offset;
		this.size = size;
	}

	public int getIndexSize()
	{
		return indexSize;
	}

	public void setIndexSize( int indexSize )
	{
		this.indexSize = indexSize;
	}

	public Section getSection()
	{
		return section;
	}

	public int getOffset()
	{
		return offset;
	}

	public int getSize()
	{
		return size;
	}
}
