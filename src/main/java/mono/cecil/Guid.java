package mono.cecil;

import java.util.Arrays;

public class Guid
{
	public static final int SIZE = 16;
	private final byte[] bytes;

	public Guid()
	{
		bytes = Utils.EMPTY_BYTE_ARRAY;
	}

	public Guid( byte[] bytes )
	{
		if( bytes.length != SIZE )
			throw new IllegalArgumentException( "Should contain exactly 16 bytes." );
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.bytes = bytes;
	}

	public Guid( String guid_value )
	{
		this();
		// FIXME: parse arg
	}

	public static Guid create()
	{
		// TODO: new random guid
		return new Guid();
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof Guid ) ) return false;

		Guid guid = (Guid)obj;

		//noinspection AccessingNonPublicFieldOfAnotherObject
		return Arrays.equals( bytes, guid.bytes );

	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( bytes );
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( '{' );
		boolean first = true;
		int index = 0;
		for( byte b : bytes )
		{
			if( first )
				first = false;
			else if( index % 4 == 0 )
				sb.append( '-' );

			sb.append( Integer.toHexString( b & Utils.BYTE_MASK ) );
			index++;
		}
		sb.append( '}' );
		return sb.toString();
	}
}
