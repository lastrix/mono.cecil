package mono.cecil;


import mono.cecil.metadata.MetadataToken;
import mono.cecil.pe.Image;
import mono.cecil.pe.ImageReader;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings({"NestedAssignment", "AssignmentOrReturnOfFieldWithMutableType", "WeakerAccess", "unused", "ConstantConditions"})
public final class ModuleDefinition extends ModuleReference implements ICustomAttributeProvider, IDisposable {
    private ModuleDefinition() {
        metadata = new MetadataSystem();
        setMetadataToken(new MetadataToken(TokenType.Module, 1));
    }

    public ModuleDefinition(Image image) {
        this();
        this.image = image;
        kind = image.getKind();
        setRuntimeVersion(image.getRuntimeVersion());
        architecture = image.getArchitecture();
        attributes = image.getAttributes();
        characteristics = image.getCharacteristics();
        fqName = image.getFileName();

        reader = new MetadataReader(this);
    }

    private Image image;
    private final MetadataSystem metadata;
    private ReadingMode readingMode;
    //private ISymbolReaderProvider symbolReaderProvider;

    //private ISymbolReader symbolReader;
    private IAssemblyResolver assemblyResolver;
    private IMetadataResolver metadataResolver;
    private TypeSystem typeSystem;

    private MetadataReader reader;
    private String fqName;

    private String runtimeVersion;
    private ModuleKind kind;
    private TargetRuntime runtime;
    private TargetArchitecture architecture;
    private int attributes;
    private int characteristics;
    private Guid mvid;

    private AssemblyDefinition assembly;
    private MethodDefinition entryPoint;

    private Collection<CustomAttribute> customAttributes;
    private Collection<AssemblyNameReference> assemblyReferences;
    private List<ModuleReference> modules;
    private Collection<Resource> resources;
    private Collection<ExportedType> exportedTypes;
    private TypeDefinitionCollection types;

    public ReadingMode getReadingMode() {
        return readingMode;
    }

    public Image getImage() {
        return image;
    }

    public MetadataSystem getMetadata() {
        return metadata;
    }

    public boolean isMain() {
        return kind != ModuleKind.NetModule;
    }

    public ModuleKind getKind() {
        return kind;
    }

    public void setKind(ModuleKind kind) {
        this.kind = kind;
    }

    public TargetRuntime getRuntime() {
        return runtime;
    }

    public void setRuntime(TargetRuntime runtime) {
        this.runtime = runtime;
        runtimeVersion = runtime.getVersionString();
    }

    public String getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setRuntimeVersion(String runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
        runtime = TargetRuntime.parseRuntime(runtimeVersion);
    }

    public TargetArchitecture getArchitecture() {
        return architecture;
    }

    public void setArchitecture(TargetArchitecture architecture) {
        this.architecture = architecture;
    }

    public int getAttributes() {
        return attributes;
    }

    public void setAttributes(int attributes) {
        this.attributes = attributes;
    }

    public int getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(int characteristics) {
        this.characteristics = characteristics;
    }

    public String getFullyQualifiedName() {
        return fqName;
    }

    public Guid getMvid() {
        return mvid;
    }

    public void setMvid(Guid mvid) {
        this.mvid = mvid;
    }

    public boolean hasImage() {
        return image != null;
    }

//	public boolean hasSymbols()
//	{
//		//return symbolReader != null;
//		throw new UnsupportedOperationException();
//	}

//	public ISymbolReader getSymbolReader()
//	{
//		return symbol_reader;
//	}

    @Override
    public MetadataScopeType getMetadataScopeType() {
        return MetadataScopeType.ModuleDefinition;
    }

    public AssemblyDefinition getAssembly() {
        return assembly;
    }

    public void setAssembly(AssemblyDefinition assembly) {
        this.assembly = assembly;
    }

    public IAssemblyResolver getAssemblyResolver() {
        if (assemblyResolver == null)
            assemblyResolver = AssemblyResolvers.createAssemblyResolver();
        return assemblyResolver;
    }

    public void setAssemblyResolver(IAssemblyResolver assemblyResolver) {
        this.assemblyResolver = assemblyResolver;
    }

    public IMetadataResolver getMetadataResolver() {
        if (metadataResolver == null)
            metadataResolver = new MetadataResolver(getAssemblyResolver());
        return metadataResolver;
    }

    public void setMetadataResolver(IMetadataResolver metadataResolver) {
        this.metadataResolver = metadataResolver;
    }

    public TypeSystem getTypeSystem() {
        if (typeSystem == null)
            typeSystem = TypeSystem.createTypeSystem(this);
        return typeSystem;
    }

    public boolean hasAssemblyReferences() {
        if (assemblyReferences != null)
            return !assemblyReferences.isEmpty();
        return hasImage() && image.hasTable(Table.AssemblyRef);
    }

    public Collection<AssemblyNameReference> getAssemblyReferences() {
        if (assemblyReferences != null)
            return assemblyReferences;

        if (hasImage())
            return assemblyReferences = read(this, (reader1, item) -> reader1.readAssemblyReferences());
        return assemblyReferences = new ArrayList<>();
    }

    public boolean hasModuleReferences() {
        if (modules != null)
            return !modules.isEmpty();

        return hasImage() && image.hasTable(Table.ModuleRef);
    }

    public List<ModuleReference> getModuleReferences() {
        if (modules != null)
            return modules;

        if (hasImage())
            return modules = read(this, (reader1, item) -> reader1.readModuleReferences());

        return modules = new ArrayList<>();
    }

    public boolean hasResources() {
        if (resources != null)
            return !resources.isEmpty();

        //noinspection SimplifiableIfStatement
        if (hasImage())
            return image.hasTable(Table.ManifestResource) || read(this, (reader1, item) -> reader1.hasFileResource());

        return false;
    }

    public Collection<Resource> getResources() {
        if (resources != null)
            return resources;

        if (hasImage())
            return resources = read(this, (reader1, item) -> reader1.readResources());

        return resources = new ArrayList<>();
    }

    @Override
    public boolean hasCustomAttributes() {
        if (customAttributes != null)
            return !customAttributes.isEmpty();

        return Utils.hasCustomAttributes(this, this);
    }

    @Override
    public Collection<CustomAttribute> getCustomAttributes() {
        if (customAttributes != null)
            return customAttributes;

        return customAttributes = Utils.getCustomAttributes(this, this);
    }

    public boolean hasTypes() {
        if (types != null)
            return !types.isEmpty();

        return hasImage() && image.hasTable(Table.TypeDef);
    }

    public Collection<TypeDefinition> getTypes() {
        if (types != null)
            return types;

        if (hasImage())
            return types = read(this, (reader1, item) -> reader1.readTypes());

        return types = new TypeDefinitionCollection(this);
    }

    public Collection<TypeDefinition> getTypesByRegExp(String regexp) {
        Collection<TypeDefinition> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(regexp);
        for (TypeDefinition definition : getTypes())
            if (pattern.matcher(definition.getFullName()).matches())
                result.add(definition);

        return result;
    }

    public boolean hasExportedTypes() {
        if (exportedTypes != null)
            return !exportedTypes.isEmpty();

        return hasImage() && image.hasTable(Table.ExportedType);
    }

    public Collection<ExportedType> getExportedTypes() {
        if (exportedTypes != null)
            return exportedTypes;

        if (hasImage())
            return exportedTypes = read(this, (reader1, item) -> reader1.readExportedTypes());

        return exportedTypes = Collections.emptyList();
    }

    @Nullable
    public MethodDefinition getEntryPoint() {
        if (entryPoint != null)
            return entryPoint;

        if (hasImage())
            //noinspection ConstantConditions
            return entryPoint = read(this, (reader1, item) -> reader1.readEntryPoint());
        return entryPoint = null;
    }

    public void setEntryPoint(MethodDefinition entryPoint) {
        this.entryPoint = entryPoint;
    }

    ////
    public boolean hasTypeReference(String fullName) {
        return hasTypeReference(null, fullName);
    }

    public boolean hasTypeReference(@Nullable String scope, String fullName) {
        if (StringUtils.isBlank(fullName))
            throw new IllegalArgumentException();

        //noinspection SimplifiableIfStatement
        if (!hasImage())
            return false;

        return getTypeReferenceImpl(scope, fullName) != null;
    }

    @Nullable
    public TypeReference getTypeReference(String scope, String fullName) {
        if (StringUtils.isBlank(fullName))
            throw new IllegalArgumentException();

        if (!hasImage())
            return null;

        return getTypeReferenceImpl(scope, fullName);
    }

    public Collection<TypeReference> getTypeReferences() {
        if (!hasImage())
            return Collections.emptyList();

        return read(this, (reader1, item) -> reader1.getTypeReferences());
    }

    public Collection<MemberReference> getMemberReferences() {
        if (!hasImage())
            return Collections.emptyList();

        return read(this, (reader1, item) -> reader1.getMemberReferences());
    }

    @Nullable
    public TypeDefinition getType(String fullName) {
        if (StringUtils.isBlank(fullName))
            throw new IllegalArgumentException();

        //noinspection HardcodedFileSeparator
        int position = fullName.indexOf('/');
        if (position > 0)
            return getNestedType(fullName);

        return ((TypeDefinitionCollection) getTypes()).getType(fullName);
    }

    public TypeDefinition getType(String namespace, String name) {
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException();

        return ((TypeDefinitionCollection) getTypes()).getType(namespace, name);
    }

    public Collection<TypeDefinition> getAllTypes() {
        Collection<TypeDefinition> result = new ArrayList<>();
        getAllTypesImpl(getTypes(), result);
        return result;
    }

    public FieldDefinition resolve(FieldReference reference) {
        return metadataResolver.resolve(reference);
    }

    public MethodDefinition resolve(MethodReference reference) {
        return metadataResolver.resolve(reference);
    }

    public TypeDefinition resolve(TypeReference reference) {
        if (metadataResolver == null)
            throw new IllegalStateException();
        return metadataResolver.resolve(reference);
    }

    public IMetadataTokenProvider lookupToken(int token) {
        return lookupToken(new MetadataToken(token));
    }

    public IMetadataTokenProvider lookupToken(final MetadataToken metadataToken) {
        //noinspection ConstantConditions
        return read(this, (reader1, item) -> reader1.lookupToken(metadataToken));
    }

    public boolean hasDebugHeader() {
        return image != null && !image.getDebug().isZero();
    }

    public boolean isCorlib() {
        return assembly != null && "mscorlib".equals(assembly.getName().getName());
    }

//	public ImageDebugDirectory GetDebugHeader (out byte [] header)
//	{
//		if (!HasDebugHeader)
//			throw new InvalidOperationException ();
//
//		return Image.GetDebugHeader (out header);
//	}
//
//	void ProcessDebugHeader ()
//	{
//		if (!HasDebugHeader)
//			return;
//
//		byte [] header;
//		var directory = GetDebugHeader (out header);
//
//		if (!symbol_reader.ProcessDebugHeader (directory, header))
//			throw new InvalidOperationException ();
//	}

//	public void ReadSymbols ()
//	{
//		if (string.IsNullOrEmpty (fq_name))
//			throw new InvalidOperationException ();
//
//		var provider = SymbolProvider.GetPlatformReaderProvider ();
//		if (provider == null)
//			throw new InvalidOperationException ();
//
//		ReadSymbols (provider.GetSymbolReader (this, fq_name));
//	}
//
//	public void ReadSymbols (ISymbolReader reader)
//	{
//		if (reader == null)
//			throw new ArgumentNullException ("reader");
//
//		symbol_reader = reader;
//
//		ProcessDebugHeader ();
//	}

    /////
    /////
    /////

    public <I, R> R read(I item, Callback<R, I> callback) {
        int position = reader.position();
        IGenericContext context = reader.getContext();

        R ret = callback.invoke(reader, item);

        reader.offset(position);
        reader.setContext(context);

        return ret;
    }

    /////
    /////
    /////

    @Nullable
    private TypeReference getTypeReferenceImpl(@Nullable final String scope, final String fullName) {
        //noinspection ConstantConditions
        return read(this, (reader1, item) -> reader1.getTypeReference(scope, fullName));
    }

    private static void getAllTypesImpl(Iterable<TypeDefinition> types, Collection<TypeDefinition> result) {
        for (TypeDefinition type : types) {
            result.add(type);

            if (type.hasNestedTypes())
                getAllTypesImpl(type.getNestedTypes(), result);
        }
    }

    @Nullable
    private TypeDefinition getNestedType(String fullName) {
        String[] names = fullName.split("/");
        TypeDefinition type = getType(names[0]);
        if (type == null)
            return null;

        boolean first = true;
        for (String name : names) {
            if (first) {
                first = false;
                continue;
            }

            type = type.getNestedType(name);

            if (type == null)
                return null;
        }

        return type;
    }

    public static ModuleDefinition readModule(String filename) {
        return readModule(filename, new ReaderParameters(ReadingMode.Deferred));
    }

    public static ModuleDefinition readModule(String filename, ReaderParameters readerParameters) {
        try {
            return readModule(filename, Utils.readFileContent(filename), readerParameters);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ModuleDefinition readModule(String filename, byte[] bytes, ReaderParameters readerParameters) {
        return ModuleReader.createModuleFrom(ImageReader.readImageFromBytes(filename, bytes), readerParameters);
    }

    public static ModuleDefinition createModule(String name, ModuleKind kind) {
        ModuleParameters parameters = new ModuleParameters();
        parameters.setKind(kind);
        return createModule(name, parameters);
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "MagicNumber"})
    public static ModuleDefinition createModule(String name, ModuleParameters parameters) {
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException();
        if (parameters == null)
            throw new IllegalArgumentException();
        ModuleDefinition module = new ModuleDefinition();
        module.setName(name);
        module.setKind(parameters.getKind());
        module.setRuntime(parameters.getTargetRuntime());
        module.setArchitecture(parameters.getArchitecture());
        module.setMvid(Guid.create());
        module.setAttributes(ModuleAttributes.ILOnly.getMask());
        module.setCharacteristics(0x8540);


        if (parameters.getAssemblyResolver() != null)
            module.assemblyResolver = parameters.getAssemblyResolver();

        if (parameters.getMetadataResolver() != null)
            module.metadataResolver = parameters.getMetadataResolver();

        if (parameters.getKind() != ModuleKind.NetModule) {
            AssemblyDefinition assembly = new AssemblyDefinition();
            module.assembly = assembly;
            module.assembly.setName(createAssemblyName(name));
            assembly.setMainModule(module);
        }

        module.getTypes().add(new TypeDefinition("", "<Module>", TypeAttributes.NotPublic.getValue()));

        return module;
    }

    static AssemblyNameDefinition createAssemblyName(String name) {
        if (name.endsWith(".dll") || name.endsWith(".exe"))
            name = name.substring(0, name.length() - 4);

        return new AssemblyNameDefinition(name, new Version(0, 0, 0, 0));
    }

    @Override
    public void dispose() {
        if (assemblyResolver != null) {
            assemblyResolver.dispose();
            assemblyResolver = null;
        }
        typeSystem = null;
        customAttributes = null;
        assemblyReferences = null;
        resources = null;
        exportedTypes = null;
        image = null;
        if (types != null) {
            types.forEach(IDisposable::dispose);
            types.clear();
            types = null;
        }
        if (reader != null) {
            reader.dispose();
            reader = null;
        }
    }
}
