package mono.cecil;

public class PointerType extends TypeSpecification
{
	public PointerType( TypeReference type )
	{
		super( type );
		setEtype( ElementType.Ptr );
	}

	@Override
	public String getName()
	{
		return super.getName() + '*';
	}

	@Override
	public String getFullName()
	{
		return super.getFullName() + '*';
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
	public boolean isPointer()
	{
		return true;
	}
}
