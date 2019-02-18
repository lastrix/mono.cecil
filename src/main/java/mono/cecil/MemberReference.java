package mono.cecil;

import mono.cecil.metadata.MetadataToken;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings( "ClassReferencesSubclass" )
public abstract class MemberReference implements IMetadataTokenProvider
{
	protected MemberReference()
	{
	}

	protected MemberReference( String name )
	{
		this.name = StringUtils.isBlank( name ) ? Utils.EMPTY : name;
	}

	private String name;
	private TypeReference declaringType;
	private MetadataToken token;

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public TypeReference getDeclaringType()
	{
		return declaringType;
	}

	public void setDeclaringType( TypeReference declaringType )
	{
		this.declaringType = declaringType;
	}

	@Override
	public MetadataToken getMetadataToken()
	{
		return token;
	}

	@Override
	public void setMetadataToken( MetadataToken token )
	{
		this.token = token;
	}

	public boolean hasImage()
	{
		ModuleDefinition module = getModule();
		return module != null && module.hasImage();

	}

	public ModuleDefinition getModule()
	{
		return declaringType == null ? null : declaringType.getModule();
	}

	public boolean isDefinition()
	{
		return false;
	}

	public boolean containsGenericParameter()
	{
		return declaringType != null && declaringType.containsGenericParameter();
	}

	protected String getMemberFullName()
	{
		return declaringType == null ? name : ( declaringType.getFullName() + "::" + name );
	}

	@Override
	public String toString()
	{
		return getFullName();
	}


	public abstract String getFullName();
}
