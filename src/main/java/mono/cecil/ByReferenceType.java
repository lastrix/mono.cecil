package mono.cecil;

public class ByReferenceType extends TypeSpecification
{
	public ByReferenceType( TypeReference type )
	{
		super( type );
		setEtype( ElementType.ByRef );
	}

	@Override
	public String getName()
	{
		return super.getName() + '&';
	}

	@Override
	public String getFullName()
	{
		return super.getFullName() + '&';
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
	public boolean isByReference()
	{
		return true;
	}
}
