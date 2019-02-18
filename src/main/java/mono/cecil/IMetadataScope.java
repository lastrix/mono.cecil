package mono.cecil;

public interface IMetadataScope extends IMetadataTokenProvider
{
	MetadataScopeType getMetadataScopeType();

	String getName();

	void setName( String name );
}
