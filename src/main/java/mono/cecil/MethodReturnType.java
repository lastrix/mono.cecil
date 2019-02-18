package mono.cecil;

import mono.cecil.metadata.MetadataToken;

import java.util.Collection;

public class MethodReturnType implements IConstantProvider, ICustomAttributeProvider, IMarshalInfoProvider
{
	public MethodReturnType( IMethodSignature method )
	{
		this.method = method;
	}

	private final IMethodSignature method;
	private ParameterDefinition parameter;
	private TypeReference returnType;

	public IMethodSignature getMethod()
	{
		return method;
	}

	public TypeReference getReturnType()
	{
		return returnType;
	}

	public void setReturnType( TypeReference returnType )
	{
		this.returnType = returnType;
	}

	public ParameterDefinition getParameter()
	{
		if( parameter == null )
			parameter = new ParameterDefinition( returnType, method );
		return parameter;
	}

	@Override
	public MetadataToken getMetadataToken()
	{
		return getParameter().getMetadataToken();
	}

	@Override
	public void setMetadataToken( MetadataToken token )
	{
		getParameter().setMetadataToken( token );
	}

	public int getAttributes()
	{
		return getParameter().getAttributes();
	}

	public void setAttributes( int value )
	{
		getParameter().setAttributes( value );
	}

	@Override
	public boolean hasCustomAttributes()
	{
		return getParameter() != null && getParameter().hasCustomAttributes();
	}

	@Override
	public Collection<CustomAttribute> getCustomAttributes()
	{
		return getParameter().getCustomAttributes();
	}

	public boolean isHasDefault()
	{
		return getParameter() != null && getParameter().isHasDefault();
	}

	public void setHasDefault( boolean value )
	{
		getParameter().setHasDefault( value );
	}

	@Override
	public boolean hasConstant()
	{
		return getParameter().hasConstant();
	}

	@Override
	public void setHasConstant( boolean value )
	{
		getParameter().setHasConstant( value );
	}

	@Override
	public Object getConstant()
	{
		return getParameter().getConstant();
	}

	@Override
	public void setConstant( Object value )
	{
		getParameter().setConstant( value );
	}

	@Override
	public boolean hasMarshalInfo()
	{
		return getParameter() != null && getParameter().hasMarshalInfo();
	}

	@Override
	public MarshalInfo getMarshalInfo()
	{
		return getParameter().getMarshalInfo();
	}

	@Override
	public void setMarshalInfo( MarshalInfo info )
	{
		getParameter().setMarshalInfo( info );
	}
}
