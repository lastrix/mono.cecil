package mono.cecil;

import java.util.Collection;

public class FunctionPointerType extends TypeSpecification implements IMethodSignature
{
	public FunctionPointerType()
	{
		super( null );
		function = new MethodReference();
		function.setName( "method" );
		setEtype( ElementType.FnPtr );
	}

	private final MethodReference function;

	@Override
	public boolean getHasThis()
	{
		return function.getHasThis();
	}

	@Override
	public void setHasThis( boolean value )
	{
		function.setHasThis( value );
	}

	@Override
	public boolean isExplicitThis()
	{
		return function.isExplicitThis();
	}

	@Override
	public void setExplicitThis( boolean value )
	{
		function.setExplicitThis( value );
	}

	@Override
	public MethodCallingConvention getMethodCallingConvention()
	{
		return function.getMethodCallingConvention();
	}

	@Override
	public void setMethodCallingConvention( MethodCallingConvention value )
	{
		function.setMethodCallingConvention( value );
	}

	@Override
	public boolean hasParameters()
	{
		return function.hasParameters();
	}

	@Override
	public Collection<ParameterDefinition> getParameters()
	{
		return function.getParameters();
	}

	@Override
	public TypeReference getReturnType()
	{
		return function.getReturnType();
	}

	@Override
	public void setReturnType( TypeReference type )
	{
		function.setReturnType( type );
	}

	@Override
	public MethodReturnType getMethodReturnType()
	{
		return function.getMethodReturnType();
	}

	@Override
	public String getName()
	{
		return function.getName();
	}

//	@Override
//	public void setName( String name )
//	{
//		throw new UnsupportedOperationException();
//	}

	@Override
	public String getNamespace()
	{
		return null;
	}

//	@Override
//	public void setNamespace( String namespace )
//	{
//		throw new UnsupportedOperationException();
//	}

	@Override
	public ModuleDefinition getModule()
	{
		return getReturnType().getModule();
	}

	@Override
	public IMetadataScope getScope()
	{
		return function.getReturnType().getScope();
	}

//	@Override
//	public void setScope( IMetadataScope scope )
//	{
//		throw new UnsupportedOperationException();
//	}

	@Override
	public boolean isFunctionPointer()
	{
		return true;
	}

	@Override
	public boolean containsGenericParameter()
	{
		return function.containsGenericParameter();
	}

	@Override
	public String getFullName()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( function.getName() ).append( ' ' );
		sb.append( function.getReturnType().getFullName() ).append( " *" );
		Utils.getMethodSignatureFullName( this, sb );
		return sb.toString();
	}

	@Override
	public TypeDefinition resolve()
	{
		return null;
	}

	@Override
	public TypeReference getElementsType()
	{
		return this;
	}
}
