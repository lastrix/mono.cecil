package mono.cecil;

import mono.cecil.pe.Image;

@SuppressWarnings( "WeakerAccess" )
public final class ImmediateModuleReader extends ModuleReader
{
	public ImmediateModuleReader( Image image )
	{
		super( image, ReadingMode.Immediate );
	}

	@Override
	protected void readModule()
	{
		getModule().read(
				getModule(),
				( reader, item ) -> {
					readModuleManifest( reader );
					readModule( item );
					return item;
				} );
	}

	public static void readModule( ModuleDefinition module )
	{
		if( module.hasAssemblyReferences() )
			read( module.getAssemblyReferences() );
		if( module.hasResources() )
			read( module.getResources() );
		if( module.hasModuleReferences() )
			read( module.getModuleReferences() );
		if( module.hasTypes() )
			readTypes( module.getTypes() );
		if( module.hasExportedTypes() )
			read( module.getExportedTypes() );
		if( module.hasCustomAttributes() )
			read( module.getCustomAttributes() );

		AssemblyDefinition assembly = module.getAssembly();
		if( assembly == null )
			return;

		if( assembly.hasCustomAttributes() )
			readCustomAttributes( assembly );
		if( assembly.hasSecurityDeclarations() )
			read( assembly.getSecurityDeclarations() );
	}

	private static void readTypes( Iterable<TypeDefinition> types )
	{
		for( TypeDefinition type : types )
			readType( type );
	}

	private static void readType( TypeDefinition type )
	{
		readGenericParameters( type );

		if( type.hasInterfaces() )
			read( type.getInterfaces() );

		if( type.hasNestedTypes() )
			readTypes( type.getNestedTypes() );

		if( type.hasLayoutInfo() )
			read( type.getClassSize() );

		if( type.hasFields() )
			readFields( type );

		if( type.hasMethods() )
			readMethods( type );

		if( type.hasProperties() )
			readProperties( type );

		if( type.hasEvents() )
			readEvents( type );

		readSecurityDeclarations( type );
		readCustomAttributes( type );
	}

	private static void readGenericParameters( IGenericParameterProvider provider )
	{
		if( !provider.hasGenericParameters() )
			return;

		for( GenericParameter parameter : provider.getGenericParameters() )
		{
			if( parameter.hasConstraints() )
				read( parameter.getConstraints() );

			readCustomAttributes( parameter );
		}
	}

	private static void readSecurityDeclarations( ISecurityDeclarationProvider provider )
	{
		if( !provider.hasSecurityDeclarations() )
			return;

		for( SecurityDeclaration declaration : provider.getSecurityDeclarations() )
			read( declaration.getSecurityAttributes() );
	}

	private static void readCustomAttributes( ICustomAttributeProvider provider )
	{
		if( !provider.hasCustomAttributes() )
			return;

		for( CustomAttribute attribute : provider.getCustomAttributes() )
			read( attribute.getConstructorArguments() );
	}

	private static void readFields( TypeDefinition type )
	{
		for( FieldDefinition field : type.getFields() )
		{
			if( field.hasConstant() )
				read( field.getConstant() );

			if( field.hasFieldLayout() )
				read( field.getOffset() );

			if( field.getRva() > 0 )
				read( field.getInitialValue() );

			if( field.hasMarshalInfo() )
				read( field.getMarshalInfo() );

			readCustomAttributes( field );
		}
	}

	private static void readMethods( TypeDefinition type )
	{
		for( MethodDefinition method : type.getMethods() )
		{
			readGenericParameters( method );

			if( method.hasParameters() )
				readParameters( method );

			if( method.hasOverrides() )
				read( method.getOverrides() );

			if( method.isPInvokeImpl() )
				read( method.getPInvoke() );

			readSecurityDeclarations( method );
			readCustomAttributes( method );

			MethodReturnType return_type = method.getMethodReturnType();
			if( return_type.hasConstant() )
				read( return_type.getConstant() );

			if( return_type.hasMarshalInfo() )
				read( return_type.getMarshalInfo() );

			readCustomAttributes( return_type );
		}
	}

	private static void readParameters( IMethodSignature method )
	{
		for( ParameterDefinition parameter : method.getParameters() )
		{
			if( parameter.hasConstant() )
				read( parameter.getConstant() );

			if( parameter.hasMarshalInfo() )
				read( parameter.getMarshalInfo() );

			readCustomAttributes( parameter );
		}
	}

	private static void readProperties( TypeDefinition type )
	{
		for( PropertyDefinition property : type.getProperties() )
		{
			read( property.getGetMethod() );

			if( property.hasConstant() )
				read( property.getConstant() );

			readCustomAttributes( property );
		}
	}

	private static void readEvents( TypeDefinition type )
	{
		for( EventDefinition event : type.getEvents() )
		{
			read( event.getAddMethod() );

			readCustomAttributes( event );
		}
	}

	@SuppressWarnings( "EmptyMethod" )
	private static void read( @SuppressWarnings( "UnusedParameters" ) Object collection )
	{
		// do nothing
	}
}
