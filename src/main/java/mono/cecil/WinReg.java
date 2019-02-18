package mono.cecil;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

@SuppressWarnings( {"UtilityClassCanBeEnum", "UtilityClass"} )
public final class WinReg
{
	private WinReg()
	{
	}

	private static final WinRegImpl INSTANCE = WinRegImpl.isSupported() ? new WinRegImpl() : null;

	public static boolean isSupported()
	{
		return INSTANCE != null;
	}

	@SuppressWarnings( "SameParameterValue" )
	public static String valueForKey( int hkey, String path, String key, String defaultValue )
	{
		if( !isSupported() )
			return defaultValue;

		try
		{
			String value = valueForKey( hkey, path, key );
			return StringUtils.isBlank( value ) ? defaultValue : value;
		} catch( Exception ignored )
		{
			return defaultValue;
		}
	}

	@SuppressWarnings( "WeakerAccess" )
	@Nullable
	public static String valueForKey( int hkey, String path, String key )
			throws IllegalAccessException, InvocationTargetException, IOException
	{
		if( !isSupported() )
			throw new UnsupportedOperationException();

		return INSTANCE.valueForKey( hkey, path, key );
	}

	private static final class WinRegImpl
	{
		private WinRegImpl()
		{
			userRoot = Preferences.userRoot();
			systemRoot = Preferences.systemRoot();
			Class<? extends Preferences> userClass = userRoot.getClass();
			try
			{
				regOpenKey = userClass.getDeclaredMethod( "WindowsRegOpenKey", int.class, byte[].class, int.class );
				regOpenKey.setAccessible( true );
				regCloseKey = userClass.getDeclaredMethod( "WindowsRegCloseKey", int.class );
				regCloseKey.setAccessible( true );
				regQueryValueEx = userClass.getDeclaredMethod( "WindowsRegQueryValueEx", int.class, byte[].class );
				regQueryValueEx.setAccessible( true );
			} catch( Exception e )
			{
				throw new IllegalStateException( e );
			}
		}

		private final Preferences userRoot;
		private final Preferences systemRoot;
		private final Method regOpenKey;
		private final Method regCloseKey;
		private final Method regQueryValueEx;

		private static boolean isSupported()
		{
			String osName = System.getProperty( "os.name" );
			return !StringUtils.isBlank( osName ) && osName.toLowerCase().contains( "windows" );
		}

		@Nullable
		private String valueForKey( int hkey, String path, String key )
				throws IllegalAccessException, InvocationTargetException, IOException
		{
			if( !isSupported() )
				throw new UnsupportedOperationException();

			if( hkey == HKey.LOCAL_MACHINE.getCode() )
				return valueForKey( systemRoot, hkey, path, key );
			else if( hkey == HKey.CURRENT_USER.getCode() )
				return valueForKey( userRoot, hkey, path, key );
			else
				return valueForKey( null, hkey, path, key );
		}

		@Nullable
		private String valueForKey( @Nullable Preferences root, int hkey, String path, String key )
				throws IllegalAccessException, InvocationTargetException, IOException
		{
			int[] handles = (int[])regOpenKey.invoke( root, hkey, toCstr( path ), KEY_READ );
			if( handles[1] != REG_SUCCESS )
				//noinspection HardcodedFileSeparator
				throw new IllegalArgumentException( "The system can not find the specified path: '" + HKey.forCode( hkey ) + '\\' + path + '\'' );
			byte[] valb = (byte[])regQueryValueEx.invoke( root, handles[0], toCstr( key ) );
			regCloseKey.invoke( root, handles[0] );
			return valb != null ? parseValue( valb ) : queryValueForKey( hkey, path, key );
		}

		private static String queryValueForKey( int hkey, String path, String key ) throws IOException
		{
			return queryValuesForPath( hkey, path ).get( key );
		}

		private static Map<String, String> queryValuesForPath( int hKey, String path ) throws IOException
		{
			StringBuilder builder = new StringBuilder();
			Map<String, String> map = new HashMap<>();
			//noinspection HardcodedFileSeparator
			Process process = new ProcessBuilder( "reg", "query", HKey.forCode( hKey ) + '\\' + path ).start();

			try( BufferedReader reader = new BufferedReader( new InputStreamReader( process.getInputStream() ) ) )
			{
				String line;
				//noinspection NestedAssignment
				while( ( line = reader.readLine() ) != null )
				{
					if( !line.contains( "REG_" ) )
						continue;
					//noinspection UseOfStringTokenizer
					StringTokenizer tokenizer = new StringTokenizer( line, " \t" );
					while( tokenizer.hasMoreTokens() )
					{
						String token = tokenizer.nextToken();
						if( token.startsWith( "REG_" ) )
							builder.append( "\t " );
						else
							builder.append( token ).append( ' ' );
					}
					String[] arr = builder.toString().split( "\t" );
					map.put( arr[0].trim(), arr[1].trim() );
					builder.setLength( 0 );
				}
			}
			return map;
		}

		private static final int KEY_READ = 0x20019;
		private static final int REG_SUCCESS = 0;

		@Nullable
		private static String parseValue( byte[] bytes )
		{
			if( bytes == null )
				return null;
			String ret = new String( bytes );
			if( ret.charAt( ret.length() - 1 ) == '\0' )
				return ret.substring( 0, ret.length() - 1 );
			return ret;
		}

		private static byte[] toCstr( String str )
		{
			if( str == null )
				str = "";
			return ( str + '\0' ).getBytes();
		}
	}

	public enum HKey
	{
		CLASSES_ROOT( 0x80000000, "HKEY_CLASSES_ROOT" ),
		CURRENT_USER( 0x80000001, "HKEY_CURRENT_USER" ),
		LOCAL_MACHINE( 0x80000002, "HKEY_LOCAL_MACHINE" );

		private final int code;
		private final String name;

		HKey( int code, String name )
		{
			this.code = code;
			this.name = name;
		}

		public int getCode()
		{
			return code;
		}

		@Nullable
		public static String forCode( int code )
		{
			for( HKey hKey : HKey.values() )
				if( hKey.getCode() == code )
					return hKey.getName();

			return null;
		}

		public String getName()
		{
			return name;
		}
	}
}
