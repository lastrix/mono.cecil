package mono.cecil;


import org.jetbrains.annotations.Nullable;

public class AssemblyLinkedResource extends Resource
{
	public AssemblyLinkedResource( String name, int attributes )
	{
		super( name, attributes );
	}

	public AssemblyLinkedResource( String name, int attributes, AssemblyNameReference assembly )
	{
		super( name, attributes );
		this.assembly = assembly;
	}

	private AssemblyNameReference assembly;

	public AssemblyNameReference getAssembly()
	{
		return assembly;
	}

	public void setAssembly( @Nullable AssemblyNameReference assembly )
	{
		this.assembly = assembly;
	}

	@Override
	public ResourceType getResourceType()
	{
		return ResourceType.AssemblyLinked;
	}
}
