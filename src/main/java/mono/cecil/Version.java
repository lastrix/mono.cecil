package mono.cecil;

import java.util.regex.Pattern;

@SuppressWarnings( "ALL" )
public class Version implements Comparable<Version>
{
	public static final Pattern PATTERN = Pattern.compile( "^\\d+(\\.\\d+)*$" );
	private final int major;
	private final int minor;
	private final int build;
	private final int revision;

	public Version( int major, int minor, int build, int revision )
	{
		this.major = major;
		this.minor = minor;
		this.build = build;
		this.revision = revision;
	}

	public Version( String part )
	{
		String[] values = part.split( "\\." );
		major = values.length > 0 ? Integer.parseInt( values[0] ) : 0;
		minor = values.length > 1 ? Integer.parseInt( values[1] ) : 0;
		build = values.length > 2 ? Integer.parseInt( values[2] ) : 0;
		revision = values.length > 3 ? Integer.parseInt( values[3] ) : 0;
	}

	public int getMajor()
	{
		return major;
	}

	public int getMinor()
	{
		return minor;
	}

	public int getBuild()
	{
		return build;
	}

	public int getRevision()
	{
		return revision;
	}

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) return true;
		if( !( o instanceof Version ) ) return false;

		Version version = (Version)o;

		if( getMajor() != version.getMajor() ) return false;
		if( getMinor() != version.getMinor() ) return false;
		if( getBuild() != version.getBuild() ) return false;
		return getRevision() == version.getRevision();

	}

	@Override
	public int hashCode()
	{
		int result = getMajor();
		result = 31 * result + getMinor();
		result = 31 * result + getBuild();
		result = 31 * result + getRevision();
		return result;
	}

	@Override
	public String toString()
	{
		return major + "." + minor + '.' + build + '.' + revision;
	}

	@Override
	public int compareTo( Version o )
	{
		int result = Integer.compare( major, o.getMajor() );
		if( result != 0 )
			return result;

		result = Integer.compare( minor, o.getMinor() );
		if( result != 0 )
			return result;

		result = Integer.compare( build, o.getBuild() );
		if( result != 0 )
			return result;
		return Integer.compare( revision, o.getRevision() );
	}
}
