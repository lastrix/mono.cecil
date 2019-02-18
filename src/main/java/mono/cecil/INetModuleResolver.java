package mono.cecil;

public interface INetModuleResolver
{
	ModuleDefinition resolveNetModule( String path );

	void registerNetModule( String path, ModuleDefinition module );
}
