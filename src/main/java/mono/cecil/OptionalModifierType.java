package mono.cecil;

public class OptionalModifierType extends TypeSpecification implements IModifierType
{
	public OptionalModifierType( TypeReference modifierType, TypeReference type )
	{
		super( type );
		if( type == null || modifierType == null )
			throw new IllegalArgumentException();
		this.modifierType = modifierType;
		setEtype( ElementType.CModOpt );
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
	public boolean isOptionalModifier()
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
		return " modopt(" + modifierType + ')';
	}
}
