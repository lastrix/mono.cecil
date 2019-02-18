package mono.cecil;

import java.util.Collection;

public interface IGenericInstance extends IMetadataTokenProvider
{
	boolean hasGenericArguments();

	Collection<TypeReference> getGenericArguments();
}
