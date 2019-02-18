package mono.cecil.metadata;

import mono.cecil.TokenType;

@SuppressWarnings( "ALL" )
public class MetadataToken
{
	public static final MetadataToken ZERO = new MetadataToken( 0 );

	private final int token;

	public MetadataToken( int token )
	{
		this.token = token;
	}

	public MetadataToken( TokenType type, int rid )
	{
		this.token = type.getCode() | rid;
	}

	public MetadataToken( TokenType type )
	{
		this( type, 0 );
	}

	public int getRid()
	{
		return token & 0x00ffffff;
	}

	public TokenType getTokenType()
	{
		return TokenType.getByCode( token & 0xff000000 );
	}

	public int toInt()
	{
		return token;
	}

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) return true;
		if( !( o instanceof MetadataToken ) ) return false;

		MetadataToken that = (MetadataToken)o;

		return token == that.token;

	}

	@Override
	public int hashCode()
	{
		return token;
	}

	@Override
	public String toString()
	{
		return String.format( "[%s:0x%04d]", getTokenType().name(), getRid() );
	}
}
