package mono.cecil.metadata;


import mono.cecil.pe.Section;

public class TableHeap extends Heap
{
	private static final int TABLE_COUNT = 45;
	private long valid;
	private long sorted;
	private final Table[] tables = new Table[TABLE_COUNT];

	public TableHeap( Section section, int offset, int size )
	{
		super( section, offset, size );
	}

	public long getValid()
	{
		return valid;
	}

	public void setValid( long valid )
	{
		this.valid = valid;
	}

	public long getSorted()
	{
		return sorted;
	}

	public void setSorted( long sorted )
	{
		this.sorted = sorted;
	}

	public boolean hasTable( mono.cecil.Table table )
	{
		return ( valid & ( 1L << table.ordinal() ) ) != 0;
	}

	public Table getTable( mono.cecil.Table table )
	{
		if( tables[table.ordinal()] == null )
			tables[table.ordinal()] = new Table();
		return tables[table.ordinal()];
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "TableHeap" ).append( System.lineSeparator() );
		for( mono.cecil.Table table : mono.cecil.Table.values() )
			if( hasTable( table ) )
				sb.append( '\t' ).append( table ).append( ": " ).append( getTable( table ) ).append( System.lineSeparator() );

		return sb.toString();
	}

	@SuppressWarnings( "PublicInnerClass" )
	public static class Table
	{
		private int offset;
		private int length;
		private int rowSize;

		private Table()
		{
		}

		public Table( int offset, int length, int rowSize )
		{
			this.offset = offset;
			this.length = length;
			this.rowSize = rowSize;
		}

		public int getOffset()
		{
			return offset;
		}

		public int getLength()
		{
			return length;
		}

		public int getRowSize()
		{
			return rowSize;
		}

		public void setOffset( int offset )
		{
			this.offset = offset;
		}

		public void setLength( int length )
		{
			this.length = length;
		}

		public void setRowSize( int rowSize )
		{
			this.rowSize = rowSize;
		}

		@Override
		public String toString()
		{
			return "Table{" +
					"offset=" + offset +
					", length=" + length +
					", rowSize=" + rowSize +
					'}';
		}
	}
}
