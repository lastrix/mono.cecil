package mono.cecil.pe;

@SuppressWarnings( "ALL" )
public class BadImageFormatException extends RuntimeException
{
	public BadImageFormatException()
	{
	}

	public BadImageFormatException( String message )
	{
		super( message );
	}

	public BadImageFormatException( String message, Throwable cause )
	{
		super( message, cause );
	}
}
