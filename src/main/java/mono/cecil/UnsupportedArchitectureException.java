package mono.cecil;

@SuppressWarnings( "ALL" )
public class UnsupportedArchitectureException extends RuntimeException
{
	public UnsupportedArchitectureException()
	{
	}

	public UnsupportedArchitectureException( String message )
	{
		super( message );
	}
}
