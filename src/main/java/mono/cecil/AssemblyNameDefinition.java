package mono.cecil;

import mono.cecil.metadata.MetadataToken;

@SuppressWarnings( "ALL" )
public class AssemblyNameDefinition extends AssemblyNameReference
{
	public AssemblyNameDefinition()
	{
		this( null, null );
	}

	public AssemblyNameDefinition( String name, Version version )
	{
		super( name, version );
		setMetadataToken( new MetadataToken( TokenType.Assembly, 1 ) );
	}

	@Override
	public byte[] getHash()
	{
		return Utils.EMPTY_BYTE_ARRAY;
	}
}
