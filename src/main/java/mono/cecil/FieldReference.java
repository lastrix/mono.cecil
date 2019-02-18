package mono.cecil;

import mono.cecil.metadata.MetadataToken;

@SuppressWarnings( "ClassReferencesSubclass" )
public class FieldReference extends MemberReference
{
	public FieldReference()
	{
	}

	public FieldReference( String name, TypeReference fieldType )
	{
		super( name );
		if( fieldType == null )
			throw new IllegalArgumentException();
		this.fieldType = fieldType;
		setMetadataToken( new MetadataToken( TokenType.MemberRef ) );
	}

	public FieldReference( String name, TypeReference fieldType, TypeReference declaringType )
	{
		this( name, fieldType );
		if( declaringType == null )
			throw new IllegalArgumentException();

		setDeclaringType( declaringType );
	}

	private TypeReference fieldType;

	public TypeReference getFieldType()
	{
		return fieldType;
	}

	public void setFieldType( TypeReference fieldType )
	{
		this.fieldType = fieldType;
	}

	@Override
	public String getFullName()
	{
		return fieldType.getFullName() + ' ' + getMemberFullName();
	}

	@Override
	public boolean containsGenericParameter()
	{
		return fieldType.containsGenericParameter() || super.containsGenericParameter();
	}

	public FieldDefinition resolve()
	{
		if( getModule() == null )
			throw new UnsupportedOperationException();

		return getModule().resolve( this );
	}
}
