package mono.cecil;

import mono.cecil.pe.Image;

@SuppressWarnings( {"WeakerAccess", "unused"} )
public abstract class ModuleReader
{
	protected ModuleReader( Image image, ReadingMode readingMode )
	{
		this.image = image;
		this.readingMode = readingMode;
		module = new ModuleDefinition( image );
	}

	private final Image image;
	private final ReadingMode readingMode;
	private final ModuleDefinition module;

	public ReadingMode getReadingMode()
	{
		return readingMode;
	}

	public Image getImage()
	{
		return image;
	}

	public ModuleDefinition getModule()
	{
		return module;
	}

	protected void readModuleManifest( MetadataReader reader )
	{
		reader.populate( getModule() );

		readAssembly( reader );
	}

	protected abstract void readModule();

	private void readAssembly( MetadataReader reader )
	{
		AssemblyNameDefinition name = reader.readAssemblyNameDefinition();
		if( name == null )
		{
			getModule().setKind( ModuleKind.NetModule );
			return;
		}

		AssemblyDefinition assembly = new AssemblyDefinition();
		assembly.setName( name );

		getModule().setAssembly( assembly );
		assembly.setMainModule( getModule() );
	}


	public static ModuleDefinition createModuleFrom( Image image, ReaderParameters parameters )
	{
		ModuleReader reader = createModuleReader( image, parameters.getReadingMode() );
		ModuleDefinition module = reader.getModule();

		if( parameters.getAssemblyResolver() != null )
			module.setAssemblyResolver( parameters.getAssemblyResolver() );

		if( parameters.getMetadataResolver() != null )
			module.setMetadataResolver( parameters.getMetadataResolver() );

		reader.readModule();

		readSymbols( module, parameters );

		return module;
	}

	@SuppressWarnings( "EmptyMethod" )
	private static void readSymbols( ModuleDefinition module, ReaderParameters parameters )
	{
//		var symbol_reader_provider = parameters.SymbolReaderProvider;
//
//		if (symbol_reader_provider == null && parameters.ReadSymbols)
//			symbol_reader_provider = SymbolProvider.GetPlatformReaderProvider ();
//
//		if (symbol_reader_provider != null) {
//			module.SymbolReaderProvider = symbol_reader_provider;
//
//			var reader = parameters.SymbolStream != null
//					? symbol_reader_provider.GetSymbolReader (module, parameters.SymbolStream)
//					: symbol_reader_provider.GetSymbolReader (module, module.FullyQualifiedName);
//
//			module.readSymbols (reader);
//		}
	}


	private static ModuleReader createModuleReader( Image image, ReadingMode mode )
	{
		if( mode == null )
			mode = ReadingMode.Deferred;

		switch( mode )
		{
			case Immediate:
				return new ImmediateModuleReader( image );
			case Deferred:
				return new DeferredModuleReader( image );
			default:
				throw new IllegalArgumentException();
		}
	}

}
