package mono.cecil;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings( "UtilityClass" )
public final class Utils
{
	public static final Object UNKNOWN = new Object();
	public static final String EMPTY = "";
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	public static final Object NO_VALUE = new Object();
	public static final Object NOT_RESOLVED = new Object();
	public static final int NOT_RESOLVED_MARK = -2;
	public static final int NO_DATA_MARK = -1;
	public static final int BYTE_MASK = 0x000000FF;

	private Utils()
	{
	}

	public static boolean getHasMarshalInfo( IMarshalInfoProvider provider, ModuleDefinition module )
	{
		return module.hasImage() && module.read( provider, MetadataReader :: hasMarshalInfo );
	}

	public static MarshalInfo getMarshalInfo( IMarshalInfoProvider provider, ModuleDefinition module )
	{
		if( !module.hasImage() )
			return null;

		return module.read( provider, MetadataReader :: readMarshalInfo );
	}

	public static byte[] readFileContent( String filename ) throws IOException
	{
		return readFileContent( new File( filename ) );
	}

	public static byte[] readFileContent( File file ) throws IOException
	{
		try( InputStream is = new FileInputStream( file ) )
		{
			return readStreamContent( is );
		}
	}

	public static byte[] readStreamContent( InputStream is ) throws IOException
	{
		try( ByteArrayOutputStream os = new ByteArrayOutputStream() )
		{
			//noinspection CheckForOutOfMemoryOnLargeArrayAllocation,MagicNumber
			byte[] buffer = new byte[4096];

			int read;
			//noinspection NestedAssignment
			while( ( read = is.read( buffer, 0, buffer.length ) ) != -1 )
				os.write( buffer, 0, read );

			return os.toByteArray();
		}
	}

	public static Object resolveConstant( IConstantProvider provider, ModuleDefinition module, Object constant )
	{
		if( module == null )
			return NO_VALUE;

		//noinspection ObjectEquality
		if( constant != NOT_RESOLVED )
			return constant;

		if( module.hasImage() )
			return module.read( provider, MetadataReader :: readConstant );
		else
			return NO_VALUE;
	}

	public static boolean containsGenericParameter( IGenericInstance instance )
	{
		for( TypeReference reference : instance.getGenericArguments() )
		{
			if( reference.containsGenericParameter() )
				return true;
		}

		return false;
	}

	public static void getGenericInstanceFullName( IGenericInstance instance, StringBuilder sb )
	{
		sb.append( '<' );
		boolean first = true;
		for( TypeReference reference : instance.getGenericArguments() )
		{
			if( first )
				first = false;
			else
				sb.append( ", " );
			sb.append( reference.getFullName() );
		}
		sb.append( '>' );
	}

	public static boolean hasImplicitThis( IMethodSignature signature )
	{
		return signature.getHasThis() && signature.isExplicitThis();
	}

	public static void getMethodSignatureFullName( IMethodSignature signature, StringBuilder sb )
	{
		sb.append( '(' );
		if( signature.hasParameters() )
		{
			boolean first = true;
			for( ParameterDefinition param : signature.getParameters() )
			{
				if( first )
					first = false;
				else
					sb.append( ", " );
				if( param.getParameterType().isSentinel() )
					sb.append( "...," );

				sb.append( param.getParameterType().getFullName() );
			}
		}
		sb.append( ')' );
	}

	public static boolean hasGenericParameters( IGenericParameterProvider provider, ModuleDefinition module )
	{
		return module.hasImage() && module.read( provider, MetadataReader :: hasGenericParameters );
	}

	public static List<GenericParameter> getGenericParameters( IGenericParameterProvider provider, ModuleDefinition module )
	{
		return module.hasImage() ? module.read( provider, MetadataReader :: readGenericParameters )
				: new GenericParameterCollection( provider );
	}

	public static int getSentinelPosition( IMethodSignature signature )
	{
		if( !signature.hasParameters() )
			return -1;

		int index = 0;
		for( ParameterDefinition param : signature.getParameters() )
		{
			if( param.getParameterType().isSentinel() )
				return index;
			index++;
		}

		return -1;
	}

	public static boolean hasCustomAttributes( ICustomAttributeProvider provider, ModuleDefinition module )
	{
		return module.hasImage() && module.read( provider, MetadataReader :: hasCustomAttributes );
	}

	public static Collection<CustomAttribute> getCustomAttributes( ICustomAttributeProvider provider, ModuleDefinition module )
	{
		return module.hasImage()
				? module.read( provider, MetadataReader :: readCustomAttributes )
				: new ArrayList<>();
	}

	public static boolean hasSecurityDeclarations( ISecurityDeclarationProvider provider, ModuleDefinition module )
	{
		return module.hasImage() && module.read( provider, MetadataReader :: hasSecurityDeclarations );
	}

	public static Collection<SecurityDeclaration> getSecurityDeclarations( ISecurityDeclarationProvider provider, ModuleDefinition module )
	{
		if( !module.hasImage() )
			return Collections.emptyList();

		return module.read( provider, MetadataReader :: readSecurityDeclarations );
	}
}
