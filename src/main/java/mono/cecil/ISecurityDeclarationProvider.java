package mono.cecil;

import java.util.Collection;

public interface ISecurityDeclarationProvider extends IMetadataTokenProvider
{
	boolean hasSecurityDeclarations();

	Collection<SecurityDeclaration> getSecurityDeclarations();
}
