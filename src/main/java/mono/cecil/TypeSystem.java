package mono.cecil;

import mono.cecil.metadata.rows.Row2;

import java.util.Collection;
import java.util.Objects;

@SuppressWarnings( {"unused", "WeakerAccess", "ClassReferencesSubclass"} )
public abstract class TypeSystem
{
	private TypeSystem( ModuleDefinition module )
	{
		this.module = module;
	}

	public static TypeSystem createTypeSystem( ModuleDefinition module )
	{
		if( module.isCorlib() )
			return new CoreTypeSystem( module );

		return new CommonTypeSystem( module );
	}

	private final ModuleDefinition module;

	private TypeReference type_object;
	private TypeReference type_void;
	private TypeReference type_bool;
	private TypeReference type_char;
	private TypeReference type_sbyte;
	private TypeReference type_byte;
	private TypeReference type_int16;
	private TypeReference type_uint16;
	private TypeReference type_int32;
	private TypeReference type_uint32;
	private TypeReference type_int64;
	private TypeReference type_uint64;
	private TypeReference type_single;
	private TypeReference type_double;
	private TypeReference type_intptr;
	private TypeReference type_uintptr;
	private TypeReference type_string;
	private TypeReference type_typedref;

	protected ModuleDefinition getModule()
	{
		return module;
	}

	private TypeReference lookupSystemType( String name, ElementType element_type )
	{
		TypeReference type = lookupType( "System", name );
		type.setEtype( element_type );
		return type;
	}

	private TypeReference lookupSystemValueType( String name, ElementType element_type )
	{
		TypeReference type = lookupType( "System", name );
		type.setEtype( element_type );
		type.setValueType( true );
		return type;
	}

	public IMetadataScope corlib()
	{
		//noinspection InstanceofThis
		if( this instanceof CommonTypeSystem )
			return ( (CommonTypeSystem)this ).getCorlibReference();

		return getModule();
	}

	public TypeReference getType_object()
	{
		if( type_object == null )
			type_object = lookupSystemType( "Object", ElementType.Object );
		return type_object;
	}

	public TypeReference getType_void()
	{
		if( type_void == null )
			type_void = lookupSystemType( "Void", ElementType.Void );
		return type_void;
	}

	public TypeReference getType_bool()
	{
		if( type_bool == null )
			type_bool = lookupSystemValueType( "Boolean", ElementType.Boolean );
		return type_bool;
	}

	public TypeReference getType_char()
	{
		if( type_char == null )
			type_char = lookupSystemValueType( "Char", ElementType.Char );
		return type_char;
	}

	public TypeReference getType_sbyte()
	{
		if( type_sbyte == null )
			type_sbyte = lookupSystemValueType( "SByte", ElementType.I1 );
		return type_sbyte;
	}

	public TypeReference getType_byte()
	{
		if( type_byte == null )
			type_byte = lookupSystemValueType( "Byte", ElementType.U1 );
		return type_byte;
	}

	public TypeReference getType_int16()
	{
		if( type_int16 == null )
			type_int16 = lookupSystemValueType( "Int16", ElementType.I2 );
		return type_int16;
	}

	public TypeReference getType_uint16()
	{
		if( type_uint16 == null )
			type_uint16 = lookupSystemValueType( "UInt16", ElementType.U2 );
		return type_uint16;
	}

	public TypeReference getType_int32()
	{
		if( type_int32 == null )
			type_int32 = lookupSystemValueType( "Int32", ElementType.I4 );
		return type_int32;
	}

	public TypeReference getType_uint32()
	{
		if( type_uint32 == null )
			type_uint32 = lookupSystemValueType( "UInt32", ElementType.U4 );
		return type_uint32;
	}

	public TypeReference getType_int64()
	{
		if( type_int64 == null )
			type_int64 = lookupSystemValueType( "Int64", ElementType.I8 );
		return type_int64;
	}

	public TypeReference getType_uint64()
	{
		if( type_uint64 == null )
			type_uint64 = lookupSystemValueType( "UInt64", ElementType.U8 );
		return type_uint64;
	}

	public TypeReference getType_single()
	{
		if( type_single == null )
			type_single = lookupSystemValueType( "Single", ElementType.R4 );
		return type_single;
	}

	public TypeReference getType_double()
	{
		if( type_double == null )
			type_double = lookupSystemValueType( "Double", ElementType.R8 );
		return type_double;
	}

	public TypeReference getType_intptr()
	{
		if( type_intptr == null )
			type_intptr = lookupSystemValueType( "IntPtr", ElementType.I );
		return type_intptr;
	}

	public TypeReference getType_uintptr()
	{
		if( type_uintptr == null )
			type_uintptr = lookupSystemValueType( "UIntPtr", ElementType.U );
		return type_uintptr;
	}

	public TypeReference getType_string()
	{
		if( type_string == null )
			type_string = lookupSystemType( "String", ElementType.String );
		return type_string;
	}

	public TypeReference getType_typedref()
	{
		if( type_typedref == null )
			type_typedref = lookupSystemValueType( "TypedReference", ElementType.TypedByRef );
		return type_typedref;
	}

	public abstract TypeReference lookupType( String namespace, String name );


	private static final class CoreTypeSystem extends TypeSystem
	{
		private CoreTypeSystem( ModuleDefinition module )
		{
			super( module );
		}

		@Override
		public TypeReference lookupType( String namespace, String name )
		{
			TypeReference type = lookupTypeDefinition( namespace, name );
			if( type != null )
				return type;
			type = lookupTypeForwarded( namespace, name );
			if( type != null )
				return type;

			throw new IllegalArgumentException();
		}

		private TypeReference lookupTypeDefinition( String namespace, String name )
		{
			MetadataSystem metadata = getModule().getMetadata();
			if( metadata.getTypes() == null )
				initialize( getModule().getTypes() );

			return getModule().read( new Row2<>( namespace, name ), CoreTypeSystem :: lookupTypeDefinitionImpl );
		}

		private static TypeReference lookupTypeDefinitionImpl( MetadataReader reader, Row2<String, String> item )
		{
			TypeDefinition[] types = reader.getMetadata().getTypes();

			for( int i = 0; i < types.length; i++ )
			{
				if( types[i] == null )
					types[i] = reader.getTypeDefinition( i + 1 );

				TypeDefinition type = types[i];

				if( Objects.equals( type.getNamespace(), item.getCol1() ) && Objects.equals( type.getName(), item.getCol2() ) )
					return type;
			}

			return null;
		}

		TypeReference lookupTypeForwarded( String namespace, String name )
		{
			if( !getModule().hasExportedTypes() )
				return null;

			Collection<ExportedType> exportedTypes = getModule().getExportedTypes();
			for( ExportedType type : exportedTypes )
				if( Objects.equals( type.getNamespace(), namespace ) && Objects.equals( type.getName(), name ) )
					return type.createReference();

			return null;
		}

		@SuppressWarnings( "EmptyMethod" )
		static void initialize( Object obj )
		{
		}
	}

	private static final class CommonTypeSystem extends TypeSystem
	{
		private CommonTypeSystem( ModuleDefinition module )
		{
			super( module );
		}

		private AssemblyNameReference corlib;

		@Override
		public TypeReference lookupType( String namespace, String name )
		{
			return createTypeReference( namespace, name );
		}

		private TypeReference createTypeReference( String namespace, String name )
		{
			return new TypeReference( namespace, name, getModule(), getCorlibReference() );
		}

		private AssemblyNameReference getCorlibReference()
		{
			if( corlib != null )
				return corlib;

			String mscorlib = "mscorlib";

			Collection<AssemblyNameReference> references = getModule().getAssemblyReferences();

			for( AssemblyNameReference reference : references )
			{
				if( Objects.equals( reference.getName(), mscorlib ) )
					//noinspection NestedAssignment
					return corlib = reference;
			}

			corlib = new AssemblyNameReference( mscorlib, getCorlibVersion() );
			//noinspection MagicNumber,NumericCastThatLosesPrecision
			corlib.setPublicKeyToken( new byte[]{(byte)0xb7, 0x7a, 0x5c, 0x56, 0x19, 0x34, (byte)0xe0, (byte)0x89} );

			//references.add( corlib );

			return corlib;
		}

		private Version getCorlibVersion()
		{
			if( getModule() == null || getModule().getRuntime() == null )
				throw new IllegalArgumentException();
			switch( getModule().getRuntime() )
			{
				case Net_1_0:
				case Net_1_1:
					return new Version( 1, 0, 0, 0 );
				case Net_2_0:
					return new Version( 2, 0, 0, 0 );
				case Net_4_0:
					return new Version( 4, 0, 0, 0 );
				default:
					throw new UnsupportedOperationException();
			}
		}
	}
}
