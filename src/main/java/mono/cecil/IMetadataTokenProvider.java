package mono.cecil;

import mono.cecil.metadata.MetadataToken;

public interface IMetadataTokenProvider
{
	MetadataToken getMetadataToken();

	void setMetadataToken( MetadataToken token );
}
