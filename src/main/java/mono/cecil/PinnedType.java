package mono.cecil;

public class PinnedType extends TypeSpecification
{
	public PinnedType( TypeReference type )
	{
		super( type );
		setEtype( ElementType.Pinned );
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
	public boolean isPinned()
	{
		return true;
	}
}
