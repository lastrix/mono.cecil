package mono.cecil;

import mono.cecil.pe.Image;

@SuppressWarnings( "WeakerAccess" )
public final class DeferredModuleReader extends ModuleReader
{
	public DeferredModuleReader( Image image )
	{
		super( image, ReadingMode.Deferred );
	}

	@Override
	protected void readModule()
	{
		getModule().read(
				getModule(),
				( reader, item ) -> {
					readModuleManifest( reader );
					return getModule();
				} );
	}
}
