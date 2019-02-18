package mono.cecil;

public interface IConstantProvider extends IMetadataTokenProvider
{
	boolean hasConstant();

	void setHasConstant( boolean value );

	Object getConstant();

	void setConstant( Object value );
}
