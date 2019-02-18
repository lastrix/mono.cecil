package mono.cecil;

@SuppressWarnings( {"ClassReferencesSubclass", "unused"} )
public interface IMemberDefinition extends ICustomAttributeProvider
{
	String getName();

	void setName( String name );

	String getFullName();

	boolean isSpecialName();

	void setSpecialName( boolean value );

	boolean isRuntimeSpecialName();

	void setRuntimeSpecialName( boolean value );

	TypeDefinition getDeclaringType();

	void setDeclaringType( TypeDefinition type );
}
