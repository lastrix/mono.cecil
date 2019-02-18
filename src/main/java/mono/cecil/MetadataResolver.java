package mono.cecil;

import java.util.*;

@SuppressWarnings( "StandardVariableNames" )
public class MetadataResolver implements IMetadataResolver
{
	public MetadataResolver( IAssemblyResolver assemblyResolver )
	{
		if( assemblyResolver == null )
			throw new IllegalArgumentException();
		this.assemblyResolver = assemblyResolver;
	}

	private final IAssemblyResolver assemblyResolver;

	@Override
	public TypeDefinition resolve( TypeReference type )
	{
		if( type == null )
			throw new IllegalArgumentException();

		type = type.getElementsType();

		IMetadataScope scope = type.getScope();

		if( scope == null )
			return null;

		switch( scope.getMetadataScopeType() )
		{
			case AssemblyNameReference:
				AssemblyDefinition assembly = assemblyResolver.resolve( (AssemblyNameReference)scope, new ReaderParameters( ReadingMode.Deferred, assemblyResolver ) );
				if( assembly == null )
					return null;

				return getType( assembly.getMainModule(), type );

			case ModuleDefinition:
				return getType( (ModuleDefinition)scope, type );

			case ModuleReference:
				Collection<ModuleDefinition> modules = type.getModule().getAssembly().getModules();
				for( ModuleDefinition module : modules )
				{
					if( Objects.equals( module.getName(), scope.getName() ) )
						return getType( module, type );
				}
				break;
		}
		throw new UnsupportedOperationException();
	}

	private final Deque<String> recursionControl = new LinkedList<>();

	private TypeDefinition getType( ModuleDefinition module, TypeReference reference )
	{
		TypeDefinition type = getTypeDefinition( module, reference );
		if( type != null )
			return type;

		if( !module.hasExportedTypes() )
			return null;

		for( ExportedType exportedType : module.getExportedTypes() )
		{
			if( !Objects.equals( exportedType.getName(), reference.getName() ) )
				continue;

			if( !Objects.equals( exportedType.getNamespace(), reference.getNamespace() ) )
				continue;

			if( recursionControl.contains( reference.getFullName() ) )
				continue;

			recursionControl.push( reference.getFullName() );
			try
			{
				return exportedType.resolve();
			} finally
			{
				if( !reference.getFullName().equals( recursionControl.pop() ) )
					throw new IllegalStateException( "Corrupted recursion control stack" );
			}
		}

		return null;
	}

	private static TypeDefinition getTypeDefinition( ModuleDefinition module, TypeReference type )
	{
		if( !type.isNested() )
			return module.getType( type.getNamespace(), type.getName() );

		TypeDefinition declaring_type = type.getDeclaringType().resolve();
		if( declaring_type == null )
			return null;

		return declaring_type.getNestedType( type.getTypeFullName() );
	}

	@Override
	public FieldDefinition resolve( FieldReference field )
	{
		if( field == null )
			throw new IllegalArgumentException();

		TypeDefinition type = resolve( field.getDeclaringType() );
		if( type == null )
			return null;

		if( !type.hasFields() )
			return null;

		return getField( type, field );
	}

	private FieldDefinition getField( TypeDefinition type, FieldReference reference )
	{
		while( type != null )
		{
			FieldDefinition field = getField( type.getFields(), reference );
			if( field != null )
				return field;

			if( type.getBaseType() == null )
				return null;

			type = resolve( type.getBaseType() );
		}

		return null;
	}

	private static FieldDefinition getField( Iterable<FieldDefinition> fields, FieldReference reference )
	{
		for( FieldDefinition field : fields )
		{
			if( !Objects.equals( field.getName(), reference.getName() ) )
				continue;

			if( !areSame( field.getFieldType(), reference.getFieldType() ) )
				continue;

			return field;
		}
		return null;
	}

	@Override
	public MethodDefinition resolve( MethodReference method )
	{
		if( method == null )
			throw new IllegalArgumentException();

		TypeDefinition type = resolve( method.getDeclaringType() );
		if( type == null )
			return null;

		method = method.getElementsMethod();

		if( !type.hasMethods() )
			return null;

		return getMethod( type, method );
	}

	private MethodDefinition getMethod( TypeDefinition type, MethodReference reference )
	{
		while( type != null )
		{
			MethodDefinition method = getMethod( type.getMethods(), reference );
			if( method != null )
				return method;

			if( type.getBaseType() == null )
				return null;

			type = resolve( type.getBaseType() );
		}

		return null;
	}

	private static MethodDefinition getMethod( Iterable<MethodDefinition> methods, MethodReference reference )
	{
		for( MethodDefinition method : methods )
		{
			if( method.getName().equals( reference.getName() ) )
				continue;

			if( method.hasGenericParameters() != reference.hasGenericParameters() )
				continue;

			if( method.hasGenericParameters() && method.getGenericParameters().size() != reference.getGenericParameters().size() )
				continue;

			if( !areSame( method.getReturnType(), reference.getReturnType() ) )
				continue;

			if( method.hasParameters() != reference.hasParameters() )
				continue;

			if( !method.hasParameters() && !reference.hasParameters() )
				return method;

			if( !areSame( method.getParameters(), reference.getParameters() ) )
				continue;

			return method;
		}
		return null;
	}

	private static boolean areSame( Collection<ParameterDefinition> a, Collection<ParameterDefinition> b )
	{
		if( a.size() != b.size() )
			return false;

		Iterator<ParameterDefinition> ai = a.iterator();
		Iterator<ParameterDefinition> bi = b.iterator();
		while( ai.hasNext() && bi.hasNext() )
			if( !areSame( ai.next().getParameterType(), bi.next().getParameterType() ) )
				return false;

		return !ai.hasNext() && !bi.hasNext();
	}

	private static boolean areSame( TypeSpecification a, TypeSpecification b )
	{
		if( !areSame( a.getElementType(), b.getElementType() ) )
			return false;

		if( a.isGenericInstance() )
			return areSame( (GenericInstanceType)a, (GenericInstanceType)b );

		if( a.isRequiredModifier() || a.isOptionalModifier() )
			return areSame( (IModifierType)a, (IModifierType)b );

		//noinspection SimplifiableIfStatement
		if( a.isArray() )
			return areSame( (ArrayType)a, (ArrayType)b );

		return true;
	}

	private static boolean areSame( ArrayType a, ArrayType b )
	{
		if( a.getRank() != b.getRank() )
			return false;

		// TODO: dimensions

		return true;
	}

	private static boolean areSame( IModifierType a, IModifierType b )
	{
		return areSame( a.getModifierType(), b.getModifierType() );
	}

	@SuppressWarnings( "TypeMayBeWeakened" )
	private static boolean areSame( GenericInstanceType a, GenericInstanceType b )
	{
		Collection<TypeReference> aArgs = a.getGenericArguments();
		Collection<TypeReference> bArgs = b.getGenericArguments();
		if( aArgs.size() != bArgs.size() )
			return false;

		Iterator<TypeReference> ai = aArgs.iterator();
		Iterator<TypeReference> bi = bArgs.iterator();
		while( ai.hasNext() && bi.hasNext() )
		{
			if( !areSame( ai.next(), bi.next() ) )
				return false;
		}

		return !ai.hasNext() && !bi.hasNext();
	}

	private static boolean areSame( GenericParameter a, GenericParameter b )
	{
		return a.getPosition() == b.getPosition();
	}

	private static boolean areSame( TypeReference a, TypeReference b )
	{
		//noinspection ObjectEquality
		if( a == b )
			return true;

		if( a == null || b == null )
			return false;

		if( a.getEtype() != b.getEtype() )
			return false;

		if( a.isGenericParameter() )
			return areSame( (GenericParameter)a, (GenericParameter)b );

		if( a.isTypeSpecification() )
			return areSame( (TypeSpecification)a, (TypeSpecification)b );

		if( !Objects.equals( a.getName(), b.getName() ) || !Objects.equals( a.getNamespace(), b.getNamespace() ) )
			return false;

		//TODO: check scope

		return areSame( a.getDeclaringType(), b.getDeclaringType() );
	}
}
