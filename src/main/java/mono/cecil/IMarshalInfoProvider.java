package mono.cecil;

public interface IMarshalInfoProvider extends IMetadataTokenProvider
{
	boolean hasMarshalInfo();

	MarshalInfo getMarshalInfo();

	void setMarshalInfo( MarshalInfo info );
}
