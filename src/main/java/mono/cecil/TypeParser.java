package mono.cecil;

import mono.cecil.metadata.rows.Row2;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@SuppressWarnings( "TypeMayBeWeakened" )
public class TypeParser
{
	private TypeParser( String fullName )
	{
		this.fullName = fullName;
		length = fullName.length();
	}

	private final String fullName;
	private final int length;
	private int position;

	private Type parseType( boolean fqName )
	{
		Type type = new Type();
		type.fullName = parsePart();
		type.nestedNames = parseNestedNames();
		if( tryGetArity( type ) )
			type.genericArguments = parseGenericArguments( type.arity );

		type.specs = parseSpecs();

		if( fqName )
			type.assembly = parseAssemblyName();

		return type;
	}

	private String parsePart()
	{
		StringBuilder part = new StringBuilder();
		while( position < length && !isDelimiter( fullName.charAt( position ) ) )
		{
			//noinspection HardcodedFileSeparator
			if( fullName.charAt( position ) == '\\' )
				position++;

			part.append( fullName.charAt( position ) );
			position++;
		}

		return part.toString();
	}

	private Collection<String> parseNestedNames()
	{
		Collection<String> nestedNames = new ArrayList<>();
		while( tryParse( '+' ) )
			nestedNames.add( parsePart() );

		return nestedNames;
	}

	private boolean tryParse( char chr )
	{
		if( position < length && fullName.charAt( position ) == chr )
		{
			position++;
			return true;
		}

		return false;
	}

	private Collection<Integer> parseSpecs()
	{
		Collection<Integer> specs = new ArrayList<>();

		while( position < length )
		{
			switch( fullName.charAt( position ) )
			{
				case '*':
					position++;
					specs.add( Type.Ptr );
					break;

				case '&':
					position++;
					specs.add( Type.ByRef );
					break;

				case '[':
					position++;
					parseSpecsImpl( specs );
					break;
				default:
					return specs;
			}
		}

		return specs;
	}

	private void parseSpecsImpl( Collection<Integer> specs )
	{
		switch( fullName.charAt( position ) )
		{
			case ']':
				position++;
				specs.add( Type.szArray );
				break;

			case '*':
				position++;
				specs.add( 1 );
				break;

			default:
				int rank = 1;
				while( tryParse( ',' ) )
					rank++;

				specs.add( rank );
				tryParse( ']' );
				break;
		}
	}

	private Collection<Type> parseGenericArguments( int arity )
	{
		if( position == length || fullName.charAt( position ) != '[' )
			return Collections.emptyList();

		Collection<Type> genericArguments = new ArrayList<>();

		tryParse( '[' );

		for( int i = 0; i < arity; i++ )
		{
			boolean fq_argument = tryParse( '[' );
			genericArguments.add( parseType( fq_argument ) );
			if( fq_argument )
				tryParse( ']' );

			tryParse( ',' );
			tryParseWhiteSpace();
		}

		tryParse( ']' );

		return genericArguments;
	}

	private String parseAssemblyName()
	{
		if( !tryParse( ',' ) )
			return "";

		tryParseWhiteSpace();

		int start = position;
		while( position < length )
		{
			char chr = fullName.charAt( position );
			if( chr == '[' || chr == ']' )
				break;

			position++;
		}

		return fullName.substring( start, position );
	}

	private void tryParseWhiteSpace()
	{
		while( position < length && Character.isWhitespace( fullName.charAt( position ) ) )
			position++;
	}

	public static TypeReference parseType( ModuleDefinition module, String fullname )
	{
		if( StringUtils.isBlank( fullname ) )
			return null;

		TypeParser parser = new TypeParser( fullname );
		return getTypeReference( module, parser.parseType( true ) );
	}

	private static TypeReference getTypeReference( ModuleDefinition module, Type type_info )
	{
		TypeReference type = getDefinition( module, type_info );
		if( type == null )
			type = createReference( type_info, module, getMetadataScope( module, type_info ) );

		return createSpecs( type, type_info );
	}

	private static TypeReference createSpecs( TypeReference type, Type type_info )
	{
		type = createGenericInstanceType( type, type_info );

		Collection<Integer> specs = type_info.specs;
		if( specs == null || specs.isEmpty() )
			return type;

		for( Integer spec : specs )
		{
			switch( spec )
			{
				case Type.Ptr:
					type = new PointerType( type );
					break;

				case Type.ByRef:
					type = new ByReferenceType( type );
					break;

				case Type.szArray:
					type = new ArrayType( type );
					break;

				default:
					ArrayType array = new ArrayType( type );
					array.clearDimensions();
					for( int j = 0; j < spec; j++ )
						array.addDimension( new ArrayDimension() );

					type = array;
					break;
			}
		}
		return type;
	}

	private static TypeReference createGenericInstanceType( TypeReference type, Type type_info )
	{
		if( type_info.genericArguments == null || type_info.genericArguments.isEmpty() )
			return type;

		GenericInstanceType instance = new GenericInstanceType( type );
		Collection<TypeReference> instanceGenericArguments = new ArrayList<>();
		instance.setGenericArguments( instanceGenericArguments );

		for( Type argument : type_info.genericArguments )
			instanceGenericArguments.add( getTypeReference( type.getModule(), argument ) );

		return instance;
	}

	private static boolean tryGetArity( Type type )
	{
		int arity = 0;

		arity += tryGetArity( type.fullName );

		for( String nestedName : type.nestedNames )
			if( !StringUtils.isBlank( nestedName ) )
				arity += tryGetArity( nestedName );

		type.arity = arity;
		return arity > 0;
	}

	private static int tryGetArity( String name )
	{
		int index = name.lastIndexOf( '`' );
		if( index == -1 )
			return 0;

		return Integer.parseInt( name.substring( index + 1 ) );
	}

	private static boolean isDelimiter( char chr )
	{
		return "+,[]*&".indexOf( chr ) != -1;
	}

	private static TypeReference createReference( Type type_info, ModuleDefinition module, IMetadataScope scope )
	{
		Row2<String, String> splits = splitFullName( type_info.fullName );

		TypeReference type = new TypeReference( splits.getCol1(), splits.getCol2(), module, scope );
		MetadataSystem.tryProcessPrimitiveTypeReference( type );

		adjustGenericParameters( type );

		if( type_info.nestedNames == null || type_info.nestedNames.isEmpty() )
			return type;

		for( String nestedName : type_info.nestedNames )
		{
			TypeReference newType = new TypeReference( "", nestedName, module, null );
			newType.setDeclaringType( type );
			type = newType;

			adjustGenericParameters( type );
		}

		return type;
	}

	private static void adjustGenericParameters( TypeReference type )
	{
		int arity = tryGetArity( type.getName() );
		if( arity == 0 )
			return;

		for( int i = 0; i < arity; i++ )
			type.getGenericParameters().add( new GenericParameter( type ) );
	}

	private static IMetadataScope getMetadataScope( ModuleDefinition module, Type type_info )
	{
		if( StringUtils.isBlank( type_info.assembly ) )
			return module.getTypeSystem().corlib();

		return matchReference( module, AssemblyNameReference.parse( type_info.assembly ) );
	}

	private static AssemblyNameReference matchReference( ModuleDefinition module, AssemblyNameReference pattern )
	{
		Collection<AssemblyNameReference> references = module.getAssemblyReferences();

		for( AssemblyNameReference reference : references )
		{
			if( Objects.equals( reference.getFullName(), pattern.getFullName() ) )
				return reference;
		}

		return pattern;
	}

	private static TypeReference getDefinition( ModuleDefinition module, Type type_info )
	{
		if( !currentModule( module, type_info ) )
			return null;

		TypeDefinition typedef = module.getType( type_info.fullName );
		if( typedef == null )
			return null;

		if( type_info.nestedNames == null )
			return null;

		for( String nestedName : type_info.nestedNames )
			typedef = typedef.getNestedType( nestedName );

		return typedef;
	}

	private static boolean currentModule( ModuleDefinition module, Type type_info )
	{
		//noinspection SimplifiableIfStatement
		if( StringUtils.isBlank( type_info.assembly ) )
			return true;

		return module.getAssembly() != null && Objects.equals( module.getAssembly().getName().getFullName(), type_info.assembly );

	}

	@SuppressWarnings( "unused" )
	public static String toParseable( TypeReference type )
	{
		if( type == null )
			return null;

		StringBuilder name = new StringBuilder();
		appendType( type, name, true, true );
		return name.toString();
	}

	private static void appendNamePart( CharSequence part, StringBuilder name )
	{
		for( int i = 0; i < part.length(); i++ )
		{
			char chr = part.charAt( i );
			if( isDelimiter( chr ) )
				//noinspection HardcodedFileSeparator
				name.append( '\\' );
			name.append( chr );
		}
	}

	private static void appendType( TypeReference type, StringBuilder name, boolean fq_name, boolean top_level )
	{
		TypeReference declaring_type = type.getDeclaringType();
		if( declaring_type != null )
		{
			appendType( declaring_type, name, false, top_level );
			name.append( '+' );
		}

		String namespace = type.getNamespace();
		if( !StringUtils.isBlank( namespace ) )
		{
			appendNamePart( namespace, name );
			name.append( '.' );
		}

		appendNamePart( type.getElementsType().getName(), name );

		if( !fq_name )
			return;

		if( type.isTypeSpecification() )
			appendTypeSpecification( (TypeSpecification)type, name );

		if( requiresFullyQualifiedName( type, top_level ) )
		{
			name.append( ", " );
			name.append( getScopeFullName( type ) );
		}
	}

	private static String getScopeFullName( TypeReference type )
	{
		IMetadataScope scope = type.getScope();
		switch( scope.getMetadataScopeType() )
		{
			case AssemblyNameReference:
				return ( (AssemblyNameReference)scope ).getFullName();

			case ModuleDefinition:
				return ( (ModuleDefinition)scope ).getAssembly().getName().getFullName();

			default:
				throw new IllegalArgumentException();
		}
	}

	private static void appendTypeSpecification( TypeSpecification type, StringBuilder name )
	{
		if( type.getElementType().isTypeSpecification() )
			appendTypeSpecification( (TypeSpecification)type.getElementType(), name );

		switch( type.getEtype() )
		{
			case Ptr:
				name.append( '*' );
				break;
			case ByRef:
				name.append( '&' );
				break;
			case SzArray:
			case Array:
				ArrayType array = (ArrayType)type;
				if( array.isVector() )
				{
					name.append( "[]" );
				}
				else
				{
					name.append( '[' );
					for( int i = 1; i < array.getRank(); i++ )
						name.append( ',' );
					name.append( ']' );
				}
				break;
			case GenericInst:
				appendGenericInstTypeSpec( (GenericInstanceType)type, name );
				break;

			default:
				// nothing to do
		}
	}

	private static void appendGenericInstTypeSpec( GenericInstanceType type, StringBuilder name )
	{
		Collection<TypeReference> arguments = type.getGenericArguments();

		name.append( '[' );
		boolean first = true;
		for( TypeReference argument : arguments )
		{
			if( first )
				first = false;
			else name.append( ',' );
			//noinspection ObjectEquality
			boolean requires_fqname = argument.getScope() != argument.getModule();
			if( requires_fqname )
				name.append( '[' );

			appendType( argument, name, true, false );

			if( requires_fqname )
				name.append( ']' );
		}

		name.append( ']' );
	}

	private static boolean requiresFullyQualifiedName( TypeReference type, boolean top_level )
	{
		//noinspection SimplifiableIfStatement,ObjectEquality
		if( type.getScope() == type.getModule() )
			return false;

		return !( type.getScope().getName().equals( "mscorlib" ) && top_level );

	}


	public static Row2<String, String> splitFullName( String fullname )
	{
		int last_dot = fullname.lastIndexOf( '.' );

		if( last_dot == -1 )
			return new Row2<>( "", fullname );

		return new Row2<>( fullname.substring( 0, last_dot ), fullname.substring( last_dot + 1 ) );
	}


	@SuppressWarnings( "PackageVisibleField" )
	private static final class Type
	{
		static final int Ptr = -1;
		static final int ByRef = -2;
		static final int szArray = -3;

		String fullName;
		Collection<String> nestedNames;
		int arity;
		Collection<Integer> specs;
		Collection<Type> genericArguments;
		String assembly;
	}
}
