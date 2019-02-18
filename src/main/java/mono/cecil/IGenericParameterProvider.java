package mono.cecil;

import java.util.Collection;

@SuppressWarnings( "ClassReferencesSubclass" )
public interface IGenericParameterProvider extends IMetadataTokenProvider
{
	boolean hasGenericParameters();

	boolean isDefinition();

	ModuleDefinition getModule();

	Collection<GenericParameter> getGenericParameters();

	GenericParameterType getGenericParameterType();

	GenericParameter getGenericParameter( int index );
}
