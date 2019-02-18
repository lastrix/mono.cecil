package mono.cecil;

import mono.cecil.metadata.MetadataToken;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings( {"AssignmentToCollectionOrArrayFieldFromParameter", "ReturnOfCollectionOrArrayField"} )
public class AssemblyNameReference implements IMetadataScope, Comparable<AssemblyNameReference>
{
	public AssemblyNameReference()
	{
	}

	public AssemblyNameReference( String name, @Nullable Version version )
	{
		this.name = name;
		this.version = version;
		hashAlgorithm = AssemblyHashAlgorithm.None;
		metadataToken = new MetadataToken( TokenType.AssemblyRef );
	}

	private String name;
	private String culture;
	private Version version;
	private int attributes;
	private byte[] publicKey;
	private byte[] publicKeyToken;
	private AssemblyHashAlgorithm hashAlgorithm;
	private byte[] hash;

	private MetadataToken metadataToken;

	private String fullName;

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName( String name )
	{
		this.name = name;
		fullName = null;
	}

	public String getCulture()
	{
		return culture;
	}

	public void setCulture( String culture )
	{
		this.culture = culture;
		fullName = null;
	}

	public Version getVersion()
	{
		return version;
	}

	public void setVersion( Version version )
	{
		this.version = version;
		fullName = null;
	}

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
	}

	public boolean isAttribute( AssemblyAttributes attribute )
	{
		return attribute.isSet( attributes );
	}

	public byte[] getPublicKey()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return publicKey;
	}

	public void setPublicKey( byte[] publicKey )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.publicKey = publicKey;
		setHasPublicKey( ArrayUtils.isEmpty( publicKey ) );
		publicKeyToken = null;
		fullName = null;
	}

	@Nullable
	@SuppressWarnings( "unused" )
	byte[] getPublicKeyToken()
	{
		if( publicKeyToken == null && ArrayUtils.isNotEmpty( publicKey ) )
		{
			byte[] publicKeyHash = hashPublicKey();
			publicKeyToken = new byte[8];
			System.arraycopy( publicKeyHash, publicKeyHash.length - 8, publicKeyToken, 0, 8 );
			ArrayUtils.reverse( publicKeyToken );
		}
		//noinspection ReturnOfCollectionOrArrayField
		return publicKeyToken;
	}

	String getPublicKeyTokenAsString()
	{
		byte[] token = getPublicKeyToken();
		if( ArrayUtils.isEmpty( token ) )
			return "null";

		StringBuilder sb = new StringBuilder();
		for( byte b : getPublicKeyToken() )
			sb.append( String.format( "%02x", b ) );
		return sb.toString();
	}

	private byte[] hashPublicKey()
	{
		MessageDigest md;
		try
		{
			md = createDigest();
		} catch( NoSuchAlgorithmException e )
		{
			throw new IllegalStateException( e );
		}
		return md.digest( publicKey );
	}

	private MessageDigest createDigest()
			throws NoSuchAlgorithmException
	{
		switch( hashAlgorithm )
		{
			case Reserved:
				return MessageDigest.getInstance( "MD5" );

			default:
				return MessageDigest.getInstance( "SHA1" );
		}
	}

	void setPublicKeyToken( @Nullable byte[] publicKeyToken )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.publicKeyToken = publicKeyToken;
		fullName = null;
	}

	@Override
	public MetadataScopeType getMetadataScopeType()
	{
		return MetadataScopeType.AssemblyNameReference;
	}

	public String getFullName()
	{
		if( fullName != null )
			return fullName;

		StringBuilder sb = new StringBuilder();
		sb.append( name );
		if( version != null )
			sb.append( ", " ).append( "Version=" ).append( version );

		sb.append( ", " ).append( "Culture=" ).append( StringUtils.isBlank( culture ) ? "neutral" : culture );

		sb.append( ", " ).append( "PublicKeyToken=" ).append( getPublicKeyTokenAsString() );

		fullName = sb.toString();
		return fullName;
	}


	@SuppressWarnings( "unused" )
	public AssemblyHashAlgorithm getHashAlgorithm()
	{
		return hashAlgorithm;
	}

	void setHashAlgorithm( AssemblyHashAlgorithm hashAlgorithm )
	{
		this.hashAlgorithm = hashAlgorithm;
	}

	public byte[] getHash()
	{
		return hash;
	}

	public void setHash( byte[] hash )
	{
		this.hash = hash;
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

	@Override
	public String toString()
	{
		return getFullName();
	}

	public String toDllName()
	{
		String name = getName();
		if( name.endsWith( ".dll" ) )
			return name;
		return name + ".dll";
	}

	public static AssemblyNameReference parse( String fullName )
	{
		if( StringUtils.isBlank( fullName ) )
			throw new IllegalArgumentException();

		AssemblyNameReference reference = new AssemblyNameReference();
		String[] tokens = fullName.split( "," );
		boolean first = true;
		for( String token : tokens )
		{
			if( first )
			{
				first = false;
				reference.setName( token );
				continue;
			}

			String[] parts = token.split( "=" );
			if( parts.length != 2 )
				throw new IllegalStateException( "Invalid token in full name: " + token );

			switch( parts[0].toLowerCase().trim() )
			{
				case "version":
					if( Version.PATTERN.matcher( parts[1] ).matches() )
						reference.setVersion( new Version( parts[1] ) );
					break;

				case "culture":
					reference.setCulture( parts[1] );
					break;

				case "publickeytoken":
					if( parts[1].equalsIgnoreCase( "null" ) )
						break;
					reference.setPublicKeyToken( parsePublicKeyToken( parts[1] ) );
					break;

				case "processorarchitecture":
					break;

				default:
					throw new IllegalStateException( parts[0] );
			}
		}
		return reference;
	}

	private static byte[] parsePublicKeyToken( String value )
	{
		byte[] bytes = new byte[value.length() / 2];
		for( int i = 0; i < bytes.length; i++ )
			//noinspection MagicNumber,NumericCastThatLosesPrecision
			bytes[i] = (byte)Integer.parseInt( value.substring( i * 2, i * 2 + 2 ), 16 );
		return bytes;
	}

	public boolean isHasPublicKey()
	{
		return AssemblyAttributes.PublicKey.isSet( attributes );
	}

	private void setHasPublicKey( boolean value )
	{
		attributes = AssemblyAttributes.PublicKey.set( value, attributes );
	}

	public boolean isSideBySideCompatible()
	{
		return AssemblyAttributes.SideBySideCompatible.isSet( attributes );
	}

	public void setSideBySideCompatible( boolean value )
	{
		attributes = AssemblyAttributes.SideBySideCompatible.set( value, attributes );
	}

	boolean isRetargetable()
	{
		return AssemblyAttributes.Retargetable.isSet( attributes );
	}

	public void setRetargetable( boolean value )
	{
		attributes = AssemblyAttributes.Retargetable.set( value, attributes );
	}

	public boolean isWindowsRuntime()
	{
		return AssemblyAttributes.WindowsRuntime.isSet( attributes );
	}

	public void setWindowsRuntime( boolean value )
	{
		attributes = AssemblyAttributes.WindowsRuntime.set( value, attributes );
	}

	@Override
	public boolean equals( Object obj )
	{
		return this == obj || obj instanceof AssemblyNameReference && toString().equals( obj.toString() );

	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public int compareTo( @NotNull AssemblyNameReference o )
	{
		return toString().compareTo( o.toString() );
	}
}
