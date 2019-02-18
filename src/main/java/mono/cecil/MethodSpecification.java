package mono.cecil;

import mono.cecil.metadata.MetadataToken;

import java.util.List;

public class MethodSpecification extends MethodReference
{
	public MethodSpecification( MethodReference method )
	{
		if( method == null )
			throw new IllegalArgumentException();
		this.method = method;
		setMetadataToken( new MetadataToken( TokenType.MethodSpec ) );
	}

	private final MethodReference method;

	public MethodReference getElementMethod()
	{
		return method;
	}

	@Override
	public String getName()
	{
		return method.getName();
	}

	@Override
	public void setName( String name )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public MethodCallingConvention getMethodCallingConvention()
	{
		return method.getMethodCallingConvention();
	}

	@Override
	public void setMethodCallingConvention( MethodCallingConvention value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getHasThis()
	{
		return method.getHasThis();
	}

	@Override
	public void setHasThis( boolean value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isExplicitThis()
	{
		return method.isExplicitThis();
	}

	@Override
	public void setExplicitThis( boolean value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public MethodReturnType getMethodReturnType()
	{
		return method.getMethodReturnType();
	}

	@Override
	public void setMethodReturnType( MethodReturnType methodReturnType )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public TypeReference getDeclaringType()
	{
		return method.getDeclaringType();
	}

	@Override
	public void setDeclaringType( TypeReference declaringType )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ModuleDefinition getModule()
	{
		return method.getModule();
	}

	@Override
	public boolean hasParameters()
	{
		return method.hasParameters();
	}

	@Override
	public List<ParameterDefinition> getParameters()
	{
		return method.getParameters();
	}

	@Override
	public boolean containsGenericParameter()
	{
		return method.containsGenericParameter();
	}

	@Override
	public MethodReference getElementsMethod()
	{
		return method.getElementsMethod();
	}
}
