package mono.cecil;

import java.util.Collection;

public interface ICustomAttributeProvider extends IMetadataTokenProvider
{
	Collection<CustomAttribute> getCustomAttributes();

	boolean hasCustomAttributes();
}
