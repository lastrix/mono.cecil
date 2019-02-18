package mono.cecil;

import java.util.Objects;

public class CustomAttributeArgument
{
	public CustomAttributeArgument( TypeReference type, Object value )
	{
		TypeReference.checkType( type );
		this.type = type;
		this.value = value;
	}

	private final TypeReference type;
	private final Object value;

	public TypeReference getType()
	{
		return type;
	}

	public Object getValue()
	{
		return value;
	}

	@SuppressWarnings( "HardcodedFileSeparator" )
	@Override
	public String toString()
	{
		if( value instanceof CustomAttributeArgument )
			return value.toString();

		if( value instanceof CustomAttributeArgument[] )
		{
			StringBuilder sb = new StringBuilder();
			sb.append( '{' );
			boolean first = true;
			for( CustomAttributeArgument argument : (CustomAttributeArgument[])value )
			{
				if( first )
					first = false;
				else
					sb.append( ',' );
				sb.append( argument );
			}
			sb.append( '}' );
			return sb.toString();
		}

		if( value instanceof TypeReference )
			return '(' + getTypeName( type ) + ')' + getTypeName( (TypeReference)value );

		return '(' + getTypeName( type ) + ')' + Objects.toString( value );
	}

	private static String getTypeName( TypeReference type )
	{
		//noinspection HardcodedFileSeparator
		String typeFullName = type.getFullName().replace( '/', '.' );
		while( typeFullName.indexOf( '`' ) > 0 )
		{
			int idx = typeFullName.indexOf( '`' );
			int stopIdx = findStopIdx( typeFullName, idx );
			if( stopIdx == -1 )
				typeFullName = typeFullName.substring( 0, idx );
			else
				typeFullName = typeFullName.substring( 0, idx ) + typeFullName.substring( stopIdx );
		}
		return typeFullName;
	}

	private static int findStopIdx( String typeFullName, int idx )
	{
		int stop = typeFullName.indexOf( '<', idx );
		if( stop != -1 )
			return stop;
		stop = typeFullName.indexOf( '.', idx );
		return stop > idx ? stop : -1;
	}
}
