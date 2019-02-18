package mono.cecil;

import mono.cecil.pe.BadImageFormatException;
import org.apache.commons.lang3.StringUtils;

public enum TargetRuntime
{
	Unknown( null ),
	Net_1_0( "v1.0.3705" ),
	Net_1_1( "v1.1.4322" ),
	Net_2_0( "v2.0.50727" ),
	Net_4_0( "v4.0.30319" );

	private final String versionString;

	TargetRuntime( String versionString )
	{
		this.versionString = versionString;
	}

	public static TargetRuntime parseRuntime( CharSequence value )
	{
		if( StringUtils.isBlank( value ) )
			throw new BadImageFormatException();
		switch( value.charAt( 1 ) )
		{
			case '1':
				return value.charAt( 3 ) == '0' ? Net_1_0 : Net_1_1;

			case '2':
				return Net_2_0;

			case '4':
				return Net_4_0;

			default:
				return Net_4_0;
		}
	}

	public String getVersionString()
	{
		return versionString;
	}
}
