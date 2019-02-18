package mono.cecil;

import mono.cecil.metadata.MetadataToken;
import mono.cecil.pe.ByteBuffer;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings( {"unused", "WeakerAccess", "AccessingNonPublicFieldOfAnotherObject"} )
public final class SignatureReader extends ByteBuffer
{
	public SignatureReader( int blob, MetadataReader reader )
	{
		super( reader.bytes() );
		this.reader = reader;

		moveToBlob( blob );
		sigLength = readCompressedUInt32();
		start = position();
		typeSystem = reader.getModule().getTypeSystem();
	}

	private final MetadataReader reader;
	private final int start;
	private final int sigLength;
	private final TypeSystem typeSystem;

	public void moveToBlob( int blob )
	{
		offset( reader.getImage().getBlobHeap().getOffset() + blob );
	}

	public MetadataToken readTypeTokenSignature()
	{
		return CodedIndex.TypeDefOrRef.getMetadataToken( readCompressedUInt32() );
	}

	public GenericParameter getGenericParameter( GenericParameterType type, int index )
	{
		IGenericContext context = reader.getContext();

		if( context == null )
			return getUnboundGenericParameter( type, index );

		IGenericParameterProvider provider;

		switch( type )
		{
			case Type:
				provider = context.getGenericParameterProviderType();
				break;

			case Method:
				provider = context.getGenericParameterProviderMethod();
				break;

			default:
				throw new UnsupportedOperationException();
		}

		if( !context.isDefinition() )
			checkGenericContext( provider, index );

		if( index >= provider.getGenericParameters().size() )
			return getUnboundGenericParameter( type, index );

		return provider.getGenericParameter( index );
	}

	public void readGenericInstanceSignature( IGenericParameterProvider provider, IGenericInstance instance )
	{
		int arity = readCompressedUInt32();
		if( !provider.isDefinition() )
			checkGenericContext( provider, arity - 1 );

		Collection<TypeReference> collection = instance.getGenericArguments();
		for( int i = 0; i < arity; i++ )
		{
			collection.add( readTypeSignature() );
		}
	}

	public ArrayType readArrayTypeSignature()
	{
		ArrayType array = new ArrayType( readTypeSignature() );
		int rank = readCompressedUInt32();

		int[] sizes = new int[readCompressedUInt32()];
		for( int i = 0; i < sizes.length; i++ )
			sizes[i] = readCompressedUInt32();

		int[] lowBounds = new int[readCompressedUInt32()];
		for( int i = 0; i < lowBounds.length; i++ )
			lowBounds[i] = readCompressedInt32();

		array.clearDimensions();

		for( int i = 0; i < rank; i++ )
		{
			Integer lower = null;

			if( i < lowBounds.length )
				lower = lowBounds[i];

			Integer upper = null;
			if( i < sizes.length )
				upper = ( lower == null ? 0 : lower ) + sizes[i] - 1;

			array.addDimension( new ArrayDimension( lower, upper ) );
		}

		return array;
	}

	public TypeReference getTypeDefOrRef( MetadataToken token )
	{
		return reader.getTypeDefOrRef( token );
	}

	public TypeReference readTypeSignature()
	{
		return readTypeSignature( ElementType.byCode( readByte() ) );
	}

	@SuppressWarnings( "MagicNumber" )
	public void readMethodSignature( IMethodSignature method )
	{
		int calling_convention = readByte();

		int has_this = 0x20;

		if( ( calling_convention & has_this ) != 0 )
		{
			method.setHasThis( true );
			calling_convention &= ~has_this;
		}

		int explicit_this = 0x40;
		if( ( calling_convention & explicit_this ) != 0 )
		{
			method.setExplicitThis( true );
			calling_convention &= ~explicit_this;
		}

		method.setMethodCallingConvention( MethodCallingConvention.getByCode( calling_convention ) );

		MethodReference genericContext = null;
		if( method instanceof MethodReference )
			genericContext = (MethodReference)method;

		if( genericContext != null && genericContext.getDeclaringType().isArray() )
			reader.setContext( genericContext );


		//noinspection MagicNumber
		if( ( calling_convention & 0x10 ) != 0 )
		{
			int arity = readCompressedUInt32();

			if( genericContext != null && !genericContext.isDefinition() )
				checkGenericContext( genericContext, arity - 1 );
		}

		int param_count = readCompressedUInt32();

		method.getMethodReturnType().setReturnType( readTypeSignature() );

		if( param_count == 0 )
			return;

		List<ParameterDefinition> parameters;

		if( method instanceof MethodReference )
		{
			parameters = new ParameterDefinitionCollection( param_count, method );
			( (MethodReference)method ).setParameters( parameters );
		}
		else
			parameters = (List<ParameterDefinition>)method.getParameters();

		for( int i = 0; i < param_count; i++ )
			parameters.add( new ParameterDefinition( readTypeSignature() ) );
	}

	public Object readConstantSignature( ElementType type )
	{
		return readPrimitiveValue( type );
	}

	public void readCustomAttributeConstructorArguments( CustomAttribute attribute, Collection<ParameterDefinition> parameters )
	{
		int count = parameters.size();
		if( count == 0 )
			return;

		attribute.setArguments( new ArrayList<>( count ) );

		for( ParameterDefinition parameter : parameters )
			attribute.addArgument( readCustomAttributeFixedArgument( parameter.getParameterType() ) );
	}

	public void readCustomAttributeNamedArguments( int count, Collection<CustomAttributeNamedArgument> fields, Collection<CustomAttributeNamedArgument> properties )
	{
		for( int i = 0; i < count; i++ )
			readCustomAttributeNamedArgument( fields, properties );
	}

	@SuppressWarnings( "MagicNumber" )
	private void readCustomAttributeNamedArgument( Collection<CustomAttributeNamedArgument> fields, Collection<CustomAttributeNamedArgument> properties )
	{
		int kind = readByte();
		TypeReference type = readCustomAttributeFieldOrPropType();
		String name = readUTF8String();

		Collection<CustomAttributeNamedArgument> container;
		switch( kind )
		{
			case 0x53:
				container = fields;
				break;
			case 0x54:
				container = properties;
				break;
			default:
				throw new UnsupportedOperationException();
		}

		container.add( new CustomAttributeNamedArgument( name, readCustomAttributeFixedArgument( type ) ) );
	}

	private CustomAttributeArgument readCustomAttributeFixedArrayArgument( ArrayType type )
	{
		int length = readUInt32();

		//noinspection MagicNumber
		if( length == 0xffffffff )
			return new CustomAttributeArgument( type, null );

		if( length == 0 )
			//noinspection ZeroLengthArrayAllocation
			return new CustomAttributeArgument( type, new CustomAttributeArgument[0] );

		CustomAttributeArgument[] arguments = new CustomAttributeArgument[length];
		TypeReference element_type = type.getElementType();

		for( int i = 0; i < length; i++ )
			arguments[i] = readCustomAttributeElement( element_type );

		return new CustomAttributeArgument( type, arguments );
	}

	private CustomAttributeArgument readCustomAttributeFixedArgument( TypeReference type )
	{
		if( type.isArray() )
			return readCustomAttributeFixedArrayArgument( (ArrayType)type );

		return readCustomAttributeElement( type );
	}

	private CustomAttributeArgument readCustomAttributeElement( TypeReference type )
	{
		if( type.isArray() )
			return readCustomAttributeFixedArrayArgument( (ArrayType)type );

		return new CustomAttributeArgument(
				type,
				type.getEtype() == ElementType.Object
						? readCustomAttributeElement( readCustomAttributeFieldOrPropType() )
						: readCustomAttributeElementValue( type ) );
	}

	private Object readCustomAttributeElementValue( TypeReference type )
	{
		ElementType etype = type.getEtype();

		switch( etype )
		{
			case String:
				return readUTF8String();
			case None:
				if( type.isTypeOf( "System", "Type" ) )
					return readTypeReference();

				return readCustomAttributeEnum( type );
			default:
				return readPrimitiveValue( etype );
		}
	}

	private Object readPrimitiveValue( ElementType type )
	{
		switch( type )
		{
			case Boolean:
				return readByte() == 1;
			case I1:
				return readByte();
			case U1:
				return readByte();
			case Char:
				return readUInt16();
			case I2:
				return readInt16();
			case U2:
				return readUInt16();
			case I4:
				return readInt32();
			case U4:
				return readUInt32();
			case I8:
				return readInt64();
			case U8:
				return readUInt64();
			case R4:
				return readSingle();
			case R8:
				return readDouble();
			default:
				throw new UnsupportedOperationException( type.name() );
		}
	}

	private TypeReference getPrimitiveType( ElementType etype )
	{
		switch( etype )
		{
			case Boolean:
				return typeSystem.getType_bool();
			case Char:
				return typeSystem.getType_char();
			case I1:
				return typeSystem.getType_sbyte();
			case U1:
				return typeSystem.getType_byte();
			case I2:
				return typeSystem.getType_int16();
			case U2:
				return typeSystem.getType_uint16();
			case I4:
				return typeSystem.getType_int32();
			case U4:
				return typeSystem.getType_uint32();
			case I8:
				return typeSystem.getType_int64();
			case U8:
				return typeSystem.getType_uint64();
			case R4:
				return typeSystem.getType_single();
			case R8:
				return typeSystem.getType_double();
			case String:
				return typeSystem.getType_string();
			default:
				throw new UnsupportedOperationException( etype.name() );
		}
	}

	private TypeReference readCustomAttributeFieldOrPropType()
	{
		ElementType etype = ElementType.byCode( readByte() );

		switch( etype )
		{
			case Boxed:
				return typeSystem.getType_object();
			case SzArray:
				return new ArrayType( readCustomAttributeFieldOrPropType() );
			case Enum:
				return readTypeReference();
			case Type:
				return typeSystem.lookupType( "System", "Type" );
			default:
				return getPrimitiveType( etype );
		}
	}

	public TypeReference readTypeReference()
	{
		return TypeParser.parseType( reader.getModule(), readUTF8String() );
	}

	private Object readCustomAttributeEnum( TypeReference enum_type )
	{
		TypeDefinition type = enum_type.checkedResolve();
		if( !type.isEnum() )
			throw new IllegalArgumentException();

		return readCustomAttributeElementValue( type.getEnumUnderlyingType() );
	}

	public SecurityAttribute readSecurityAttribute()
	{
		SecurityAttribute attribute = new SecurityAttribute( readTypeReference() );

		readCompressedUInt32();

		readCustomAttributeNamedArguments( readCompressedUInt32(), attribute.getFields(), attribute.getProperties() );

		return attribute;
	}

	public MarshalInfo readMarshalInfo()
	{
		NativeType _native = readNativeType();
		switch( _native )
		{
			case Array:
				return readArrayMarshalInfo();

			case SafeArray:
				return readSafeArrayMarshalInfo();

			case FixedArray:
				return readFixedArrayMarshalInfo();

			case FixedSysString:
				return readFixedSysStringMarshalInfo();

			case CustomMarshaler:
				return readCustomMarshaler();

			default:
				return new MarshalInfo( _native );
		}
	}

	private MarshalInfo readCustomMarshaler()
	{
		CustomMarshalInfo marshaler = new CustomMarshalInfo();
		String guid_value = readUTF8String();
		marshaler.setGuid( new Guid( guid_value ) );
		marshaler.setUnmanagedType( readUTF8String() );
		marshaler.setManagedType( readTypeReference() );
		marshaler.setCookie( readUTF8String() );
		return marshaler;
	}

	private MarshalInfo readFixedSysStringMarshalInfo()
	{
		FixedSysStringMarshalInfo sys_string = new FixedSysStringMarshalInfo();
		if( canReadMore() )
			sys_string.setSize( readCompressedUInt32() );
		return sys_string;
	}

	private MarshalInfo readFixedArrayMarshalInfo()
	{
		FixedArrayMarshalInfo array = new FixedArrayMarshalInfo();
		if( canReadMore() )
			array.setSize( readCompressedUInt32() );
		if( canReadMore() )
			array.setElementType( readNativeType() );
		return array;
	}

	private MarshalInfo readSafeArrayMarshalInfo()
	{
		SafeArrayMarshalInfo array = new SafeArrayMarshalInfo();
		if( canReadMore() )
			array.setElementType( readVariantType() );
		return array;
	}

	private MarshalInfo readArrayMarshalInfo()
	{
		ArrayMarshalInfo array = new ArrayMarshalInfo();
		if( canReadMore() )
			array.setElementType( readNativeType() );
		if( canReadMore() )
			array.setSizeParameterIndex( readCompressedUInt32() );
		if( canReadMore() )
			array.setSize( readCompressedUInt32() );
		if( canReadMore() )
			array.setSizeParameterMultiplier( readCompressedUInt32() );
		return array;
	}

	private NativeType readNativeType()
	{
		return NativeType.getByCode( readByte() );
	}

	private VariantType readVariantType()
	{
		return VariantType.getByCode( readByte() );
	}

	private TypeReference readTypeSignature( ElementType elementType )
	{
		switch( elementType )
		{
			case ValueType:
				TypeReference value_type = getTypeDefOrRef( readTypeTokenSignature() );
				value_type.setValueType( true );
				return value_type;

			case Class:
				return getTypeDefOrRef( readTypeTokenSignature() );

			case Ptr:
				return new PointerType( readTypeSignature() );

			case FnPtr:
				FunctionPointerType fptr = new FunctionPointerType();
				readMethodSignature( fptr );
				return fptr;

			case ByRef:
				return new ByReferenceType( readTypeSignature() );

			case Pinned:
				return new PinnedType( readTypeSignature() );

			case SzArray:
				return new ArrayType( readTypeSignature() );

			case Array:
				return readArrayTypeSignature();

			case CModOpt:
				return new OptionalModifierType( getTypeDefOrRef( readTypeTokenSignature() ), readTypeSignature() );

			case CModReqD:
				return new RequiredModifierType( getTypeDefOrRef( readTypeTokenSignature() ), readTypeSignature() );

			case Sentinel:
				return new SentinelType( readTypeSignature() );

			case Var:
				return getGenericParameter( GenericParameterType.Type, readCompressedUInt32() );

			case MVar:
				return getGenericParameter( GenericParameterType.Method, readCompressedUInt32() );

			case GenericInst:
				return readGenericInstanceTypeSignature();

			case Object:
				return typeSystem.getType_object();

			case Void:
				return typeSystem.getType_void();

			case TypedByRef:
				return typeSystem.getType_typedref();

			case I:
				return typeSystem.getType_intptr();

			case U:
				return typeSystem.getType_uintptr();

			default:
				return getPrimitiveType( elementType );
		}
	}

	private TypeReference readGenericInstanceTypeSignature()
	{
		boolean is_value_type = readByte() == ElementType.ValueType.getCode();
		TypeReference element_type = getTypeDefOrRef( readTypeTokenSignature() );
		GenericInstanceType generic_instance = new GenericInstanceType( element_type );

		readGenericInstanceSignature( element_type, generic_instance );

		if( is_value_type )
		{
			generic_instance.setValueType( true );
			element_type.getElementsType().setValueType( true );
		}

		return generic_instance;
	}

	private GenericParameter getUnboundGenericParameter( GenericParameterType type, int index )
	{
		return new GenericParameter( index, type, reader.getModule() );
	}

	private static void checkGenericContext( IGenericParameterProvider owner, int index )
	{
		Collection<GenericParameter> collection = owner.getGenericParameters();
		for( int i = collection.size(); i <= index; i++ )
			collection.add( new GenericParameter( owner ) );
	}

	@Override
	public String readUTF8String()
	{
		byte[] buffer = bytes();
		//noinspection MagicNumber
		if( buffer[position()] == -1 )
		{
			advance( 1 );
			return null;
		}

		int length = readCompressedUInt32();
		if( length == 0 )
			return null;

		String result;
		try
		{
			result = new String( buffer, position(), buffer[position() + length - 1] == 0 ? length - 1 : length, "UTF-8" );
		} catch( UnsupportedEncodingException | ArrayIndexOutOfBoundsException e )
		{
			throw new IllegalStateException( e );
		}

		advance( length );
		return result;
	}

	public boolean canReadMore()
	{
		return position() - start < sigLength;
	}
}
