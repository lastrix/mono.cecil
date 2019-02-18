package mono.cecil;

import mono.cecil.metadata.MetadataToken;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings({"ReturnOfCollectionOrArrayField", "NestedAssignment"})
public class AssemblyDefinition implements ICustomAttributeProvider, ISecurityDeclarationProvider, IDisposable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssemblyDefinition.class);

    @SuppressWarnings("unused")
    public static AssemblyDefinition createAssembly(AssemblyNameDefinition assemblyName, String moduleName, ModuleKind kind) {
        return createAssembly(assemblyName, moduleName, new ModuleParameters().setKind(kind));
    }

    private static AssemblyDefinition createAssembly(AssemblyNameDefinition assemblyName, String moduleName, ModuleParameters parameters) {
        if (assemblyName == null || moduleName == null || parameters == null)
            throw new IllegalArgumentException();

        if (parameters.getKind() == ModuleKind.NetModule)
            throw new IllegalArgumentException("kind");

        AssemblyDefinition assembly = ModuleDefinition.createModule(moduleName, parameters).getAssembly();
        assembly.setName(assemblyName);
        return assembly;
    }

    public static AssemblyDefinition readAssembly(String fileName) {
        return readAssembly(ModuleDefinition.readModule(fileName));
    }

    public static AssemblyDefinition readAssembly(String fileName, ReaderParameters parameters) {
        return readAssembly(ModuleDefinition.readModule(fileName, parameters));
    }

    @Nullable
    public static AssemblyDefinition readAssemblySafe(String fileName, byte[] data, ReaderParameters parameters) {
        try {
            return readAssembly(ModuleDefinition.readModule(fileName, data, parameters));
        } catch (Throwable e) {
            LOGGER.warn("Unable to read assembly '{}', reason: {}", fileName, e.getMessage());
            return null;
        }
    }

    public static AssemblyDefinition readAssembly(String fileName, byte[] data, ReaderParameters parameters) {
        return readAssembly(ModuleDefinition.readModule(fileName, data, parameters));
    }

//	public static AssemblyDefinition readAssembly( Stream stream )
//	{
//		return readAssembly( ModuleDefinition.readModule( stream ) );
//	}
//
//	public static AssemblyDefinition readAssembly( Stream stream, ReaderParameters parameters )
//	{
//		return readAssembly( ModuleDefinition.readModule( stream, parameters ) );
//	}

    public static AssemblyDefinition readAssembly(ModuleDefinition module) {
        AssemblyDefinition assembly = module.getAssembly();
        if (assembly == null) {
            if (module.getKind() == ModuleKind.NetModule)
                throw new NetModuleException(module.getFullyQualifiedName());
            throw new IllegalArgumentException();
        }

        return assembly;
    }


    private AssemblyNameDefinition name;
    private ModuleDefinition mainModule;
    private Collection<ModuleDefinition> modules;
    private Collection<CustomAttribute> customAttributes;
    private Collection<SecurityDeclaration> securityDeclarations;

    public String getHash() {
        return mainModule.getImage().getHash();
    }

    public AssemblyNameDefinition getName() {
        return name;
    }

    public void setName(AssemblyNameDefinition name) {
        this.name = name;
    }

    public String getFullName() {
        return name.getFullName();
    }

    @Override
    public MetadataToken getMetadataToken() {
        return new MetadataToken(TokenType.Assembly, 1);
    }

    @Override
    public void setMetadataToken(MetadataToken token) {
        // not allowed
    }

    public Collection<ModuleDefinition> getModules() {
        if (modules != null)
            return modules;

        if (mainModule.hasImage())
            return mainModule.read(this, (reader, item) -> reader.readModules());

        modules = Collections.singletonList(mainModule);
        return modules;
    }

    public ModuleDefinition getMainModule() {
        return mainModule;
    }

    void setMainModule(ModuleDefinition mainModule) {
        this.mainModule = mainModule;
    }

    @Nullable
    @SuppressWarnings("unused")
    public MethodDefinition getEntryPoint() {
        return mainModule.getEntryPoint();
    }

    @SuppressWarnings("unused")
    public void setEntryPoint(MethodDefinition method) {
        mainModule.setEntryPoint(method);
    }

    @Override
    public boolean hasCustomAttributes() {
        if (customAttributes != null)
            return !customAttributes.isEmpty();
        return Utils.hasCustomAttributes(this, mainModule);
    }

    @Override
    public Collection<CustomAttribute> getCustomAttributes() {
        if (customAttributes != null)
            return customAttributes;

        return customAttributes = Utils.getCustomAttributes(this, mainModule);
    }

    @Override
    public boolean hasSecurityDeclarations() {
        if (securityDeclarations != null)
            return !securityDeclarations.isEmpty();
        return Utils.hasSecurityDeclarations(this, mainModule);
    }

    @Override
    public Collection<SecurityDeclaration> getSecurityDeclarations() {
        if (securityDeclarations != null)
            return securityDeclarations;

        return securityDeclarations = Utils.getSecurityDeclarations(this, mainModule);
    }

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public void dispose() {
        if (customAttributes != null)
            customAttributes.clear();
        if (securityDeclarations != null)
            securityDeclarations.clear();
        if (modules != null) {
            modules.forEach(IDisposable::dispose);
            modules.clear();
            modules = null;
            mainModule = null;
        }
        if (mainModule != null) {
            mainModule.dispose();
            mainModule = null;
        }
    }
}
