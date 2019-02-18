package mono.cecil;


public class CustomAttributeNamedArgument
{
	public CustomAttributeNamedArgument( String name, CustomAttributeArgument argument )
	{
		this.name = name;
		this.argument = argument;
	}

	private final String name;
	private final CustomAttributeArgument argument;

	public String getName()
	{
		return name;
	}

	public CustomAttributeArgument getArgument()
	{
		return argument;
	}
}
