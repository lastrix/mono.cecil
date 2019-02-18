package mono.cecil;

public interface IMetadataResolver
{
	TypeDefinition resolve( TypeReference type );

	FieldDefinition resolve( FieldReference field );

	MethodDefinition resolve( MethodReference method );
}
