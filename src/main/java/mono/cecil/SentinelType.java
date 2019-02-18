package mono.cecil;

public class SentinelType extends TypeSpecification
{
	public SentinelType( TypeReference type )
	{
		super( type );
		setEtype( ElementType.Sentinel );
	}

	@Override
	public boolean isValueType()
	{
		return false;
	}

	@Override
	public void setValueType( boolean valueType )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSentinel()
	{
		return true;
	}
}
