package mono.cecil;

public class RequiredModifierType extends TypeSpecification implements IModifierType
{
	public RequiredModifierType( TypeReference modifierType, TypeReference type )
	{
		super( type );
		if( type == null || modifierType == null )
			throw new IllegalArgumentException();
		this.modifierType = modifierType;
		setEtype( ElementType.CModReqD );
	}

	private final TypeReference modifierType;

	@Override
	public TypeReference getModifierType()
	{
		return modifierType;
	}

	@Override
	public String getName()
	{
		return super.getName() + getSuffix();
	}

	@Override
	public String getFullName()
	{
		return super.getFullName() + getSuffix();
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
	public boolean isRequiredModifier()
	{
		return true;
	}

	@Override
	public boolean containsGenericParameter()
	{
		return modifierType.containsGenericParameter() || super.containsGenericParameter();
	}

	private String getSuffix()
	{
		return " modreq(" + modifierType + ')';
	}
}
