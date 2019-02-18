package mono.cecil;

public class ModuleParameters
{
	private ModuleKind kind;
	private TargetRuntime targetRuntime;
	private TargetArchitecture architecture;
	private IAssemblyResolver assemblyResolver;
	private IMetadataResolver metadataResolver;

	public ModuleParameters()
	{
		kind = ModuleKind.Dll;
		targetRuntime = TargetRuntime.Net_4_0;
		architecture = TargetArchitecture.I386;
	}

	public ModuleKind getKind()
	{
		return kind;
	}

	public ModuleParameters setKind( ModuleKind kind )
	{
		this.kind = kind;
		return this;
	}

	public TargetRuntime getTargetRuntime()
	{
		return targetRuntime;
	}

	public ModuleParameters setTargetRuntime( TargetRuntime targetRuntime )
	{
		this.targetRuntime = targetRuntime;
		return this;
	}

	public TargetArchitecture getArchitecture()
	{
		return architecture;
	}

	public ModuleParameters setArchitecture( TargetArchitecture architecture )
	{
		this.architecture = architecture;
		return this;
	}

	public IAssemblyResolver getAssemblyResolver()
	{
		return assemblyResolver;
	}

	public ModuleParameters setAssemblyResolver( IAssemblyResolver assemblyResolver )
	{
		this.assemblyResolver = assemblyResolver;
		return this;
	}

	public IMetadataResolver getMetadataResolver()
	{
		return metadataResolver;
	}

	public ModuleParameters setMetadataResolver( IMetadataResolver metadataResolver )
	{
		this.metadataResolver = metadataResolver;
		return this;
	}
}
