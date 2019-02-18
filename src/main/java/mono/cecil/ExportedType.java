package mono.cecil;

import mono.cecil.metadata.MetadataToken;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings( "unused" )
public class ExportedType implements IMetadataTokenProvider
{
	public ExportedType( String namespace, String name, ModuleDefinition module, @Nullable IMetadataScope scope )
	{
		this.namespace = namespace;
		this.name = name;
		this.module = module;
		this.scope = scope;
	}

	private String namespace;
	private String name;
	private int attributes;
	private final IMetadataScope scope;
	private final ModuleDefinition module;
	private int identifier;
	private ExportedType declaringType;
	private MetadataToken metadataToken;

	public String getNamespace()
	{
		return namespace;
	}

	public void setNamespace( String namespace )
	{
		this.namespace = namespace;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
	}

	@Nullable
	public IMetadataScope getScope()
	{
		if( declaringType != null )
			return declaringType.getScope();
		return scope;
	}

	public ExportedType getDeclaringType()
	{
		return declaringType;
	}

	public void setDeclaringType( ExportedType declaringType )
	{
		this.declaringType = declaringType;
	}

	@Override
	public MetadataToken getMetadataToken()
	{
		return metadataToken;
	}

	@Override
	public void setMetadataToken( MetadataToken token )
	{
		metadataToken = token;
	}

	public int getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier( int identifier )
	{
		this.identifier = identifier;
	}

	public boolean isNotPublic()
	{
		return TypeAttributes.NotPublic.isSet( attributes );
	}

	public void setNotPublic( boolean value )
	{
		attributes = TypeAttributes.NotPublic.set( value, attributes );
	}

	public boolean isPublic()
	{
		return TypeAttributes.Public.isSet( attributes );
	}

	public void setPublic( boolean value )
	{
		attributes = TypeAttributes.Public.set( value, attributes );
	}

	public boolean isNestedPublic()
	{
		return TypeAttributes.NestedPublic.isSet( attributes );
	}

	public void setNestedPublic( boolean value )
	{
		attributes = TypeAttributes.NestedPublic.set( value, attributes );
	}

	public boolean isNestedPrivate()
	{
		return TypeAttributes.NestedPrivate.isSet( attributes );
	}

	public void setNestedPrivate( boolean value )
	{
		attributes = TypeAttributes.NestedPrivate.set( value, attributes );
	}

	public boolean isNestedFamily()
	{
		return TypeAttributes.NestedFamily.isSet( attributes );
	}

	public void setNestedFamily( boolean value )
	{
		attributes = TypeAttributes.NestedFamily.set( value, attributes );
	}

	public boolean isNestedAssembly()
	{
		return TypeAttributes.NestedAssembly.isSet( attributes );
	}

	public void setNestedAssembly( boolean value )
	{
		attributes = TypeAttributes.NestedAssembly.set( value, attributes );
	}

	public boolean isNestedFamilyAndAssembly()
	{
		return TypeAttributes.NestedFamANDAssem.isSet( attributes );
	}

	public void setNestedFamilyAndAssembly( boolean value )
	{
		attributes = TypeAttributes.NestedFamANDAssem.set( value, attributes );
	}

	public boolean isNestedFamilyOrAssembly()
	{
		return TypeAttributes.NestedFamORAssem.isSet( attributes );
	}

	public void setNestedFamilyOrAssembly( boolean value )
	{
		attributes = TypeAttributes.NestedFamORAssem.set( value, attributes );
	}

	public boolean isAutoLayout()
	{
		return TypeAttributes.AutoLayout.isSet( attributes );
	}

	public void setAutoLayout( boolean value )
	{
		attributes = TypeAttributes.AutoLayout.set( value, attributes );
	}

	public boolean isSequentialLayout()
	{
		return TypeAttributes.SequentialLayout.isSet( attributes );
	}

	public void setSequentialLayout( boolean value )
	{
		attributes = TypeAttributes.SequentialLayout.set( value, attributes );
	}

	public boolean isExplicitLayout()
	{
		return TypeAttributes.ExplicitLayout.isSet( attributes );
	}

	public void setExplicitLayout( boolean value )
	{
		attributes = TypeAttributes.ExplicitLayout.set( value, attributes );
	}

	public boolean isClass()
	{
		return TypeAttributes.Class.isSet( attributes );
	}

	public void setClass( boolean value )
	{
		attributes = TypeAttributes.Class.set( value, attributes );
	}

	public boolean isInterface()
	{
		return TypeAttributes.Interface.isSet( attributes );
	}

	public void setInterface( boolean value )
	{
		attributes = TypeAttributes.Interface.set( value, attributes );
	}

	public boolean isAbstract()
	{
		return TypeAttributes.Abstract.isSet( attributes );
	}

	public void setAbstract( boolean value )
	{
		attributes = TypeAttributes.Abstract.set( value, attributes );
	}

	public boolean isSealed()
	{
		return TypeAttributes.Sealed.isSet( attributes );
	}

	public void setSealed( boolean value )
	{
		attributes = TypeAttributes.Sealed.set( value, attributes );
	}

	public boolean isSpecialName()
	{
		return TypeAttributes.SpecialName.isSet( attributes );
	}

	public void setSpecialName( boolean value )
	{
		attributes = TypeAttributes.SpecialName.set( value, attributes );
	}

	public boolean isImport()
	{
		return TypeAttributes.Import.isSet( attributes );
	}

	public void setImport( boolean value )
	{
		attributes = TypeAttributes.Import.set( value, attributes );
	}

	public boolean isSerializable()
	{
		return TypeAttributes.Serializable.isSet( attributes );
	}

	public void setSerializable( boolean value )
	{
		attributes = TypeAttributes.Serializable.set( value, attributes );
	}

	public boolean isAnsiClass()
	{
		return TypeAttributes.AnsiClass.isSet( attributes );
	}

	public void setAnsiClass( boolean value )
	{
		attributes = TypeAttributes.AnsiClass.set( value, attributes );
	}

	public boolean isUnicodeClass()
	{
		return TypeAttributes.UnicodeClass.isSet( attributes );
	}

	public void setUnicodeClass( boolean value )
	{
		attributes = TypeAttributes.UnicodeClass.set( value, attributes );
	}

	public boolean isAutoClass()
	{
		return TypeAttributes.AutoClass.isSet( attributes );
	}

	public void setAutoClass( boolean value )
	{
		attributes = TypeAttributes.AutoClass.set( value, attributes );
	}

	public boolean isBeforeFieldInit()
	{
		return TypeAttributes.BeforeFieldInit.isSet( attributes );
	}

	public void setBeforeFieldInit( boolean value )
	{
		attributes = TypeAttributes.BeforeFieldInit.set( value, attributes );
	}

	public boolean isRuntimeSpecialName()
	{
		return TypeAttributes.RTSpecialName.isSet( attributes );
	}

	public void setRuntimeSpecialName( boolean value )
	{
		attributes = TypeAttributes.RTSpecialName.set( value, attributes );
	}

	public boolean isHasSecurity()
	{
		return TypeAttributes.HasSecurity.isSet( attributes );
	}

	public void setHasSecurity( boolean value )
	{
		attributes = TypeAttributes.HasSecurity.set( value, attributes );
	}

	public boolean isForwarder()
	{
		return TypeAttributes.Forwarder.isSet( attributes );
	}

	public void setForwarder( boolean value )
	{
		attributes = TypeAttributes.Forwarder.set( value, attributes );
	}

	public String getFullName()
	{
		String fullName = StringUtils.isBlank( namespace ) ? name : namespace + '.' + name;

		if( declaringType != null )
			//noinspection HardcodedFileSeparator
			return declaringType.getFullName() + '/' + fullName;

		return fullName;
	}

	@Override
	public String toString()
	{
		return getFullName();
	}

	public TypeDefinition resolve()
	{
		return module.resolve( createReference() );
	}

	TypeReference createReference()
	{
		TypeReference reference = new TypeReference( namespace, name, module, scope );
		if( declaringType != null )
			reference.setDeclaringType( declaringType.createReference() );
		return reference;
	}
}
