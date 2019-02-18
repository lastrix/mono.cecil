package mono.cecil;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ReaderParameters {
    public ReaderParameters() {
        this(ReadingMode.Deferred);
    }

    public ReaderParameters(ReadingMode readingMode) {
        this.readingMode = readingMode;
        assemblyResolver = AssemblyResolvers.createAssemblyResolver();
        metadataResolver = new MetadataResolver(assemblyResolver);
    }

    public ReaderParameters(ReadingMode readingMode, IAssemblyResolver assemblyResolver) {
        this.readingMode = readingMode;
        this.assemblyResolver = assemblyResolver;
        metadataResolver = new MetadataResolver(assemblyResolver);
    }

    private ReadingMode readingMode;
    private IAssemblyResolver assemblyResolver;
    private IMetadataResolver metadataResolver;
    //Stream symbol_stream;
    //private ISymbolReaderProvider symbolReaderProvider;
    //private boolean readSymbols;


    public ReadingMode getReadingMode() {
        return readingMode;
    }

    public void setReadingMode(ReadingMode readingMode) {
        this.readingMode = readingMode;
    }

    public IAssemblyResolver getAssemblyResolver() {
        return assemblyResolver;
    }

    public void setAssemblyResolver(IAssemblyResolver assemblyResolver) {
        this.assemblyResolver = assemblyResolver;
        setMetadataResolver(new MetadataResolver(assemblyResolver));
    }

    public IMetadataResolver getMetadataResolver() {
        return metadataResolver;
    }

    public void setMetadataResolver(IMetadataResolver metadataResolver) {
        this.metadataResolver = metadataResolver;
    }
}
