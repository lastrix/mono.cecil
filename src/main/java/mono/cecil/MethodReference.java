package mono.cecil;

import mono.cecil.metadata.MetadataToken;

import java.util.Collection;
import java.util.List;

public class MethodReference extends MemberReference implements IMethodSignature, IGenericParameterProvider, IGenericContext
{
	public MethodReference()
	{
		returnType = new MethodReturnType( this );
		setMetadataToken( new MetadataToken( TokenType.MemberRef ) );
	}

	public MethodReference( String name, TypeReference returnType )
	{
		super( name );
		if( returnType == null )
			throw new IllegalArgumentException();
		this.returnType = new MethodReturnType( this );
		this.returnType.setReturnType( returnType );
		setMetadataToken( new MetadataToken( TokenType.MemberRef ) );
	}

	public MethodReference( String name, TypeReference returnType, TypeReference declaringType )
	{
		this( name, returnType );
		if( declaringType == null )
			throw new IllegalArgumentException();

		//noinspection OverriddenMethodCallDuringObjectConstruction
		setDeclaringType( declaringType );
	}


	private List<ParameterDefinition> parameters;
	private MethodReturnType returnType;

	private boolean hasThis;
	private boolean explicitThis;

	private MethodCallingConvention callingConvention;
	protected List<GenericParameter> genericParameters;

	@Override
	public boolean getHasThis()
	{
		return hasThis;
	}

	@Override
	public void setHasThis( boolean value )
	{
		hasThis = value;
	}

	@Override
	public boolean isExplicitThis()
	{
		return explicitThis;
	}

	@Override
	public void setExplicitThis( boolean value )
	{
		explicitThis = value;
	}

	@Override
	public MethodCallingConvention getMethodCallingConvention()
	{
		return callingConvention;
	}

	@Override
	public void setMethodCallingConvention( MethodCallingConvention value )
	{
		callingConvention = value;
	}

	@Override
	public boolean hasParameters()
	{
		return parameters != null && !parameters.isEmpty();
	}

	@Override
	public List<ParameterDefinition> getParameters()
	{
		if( parameters == null )
			parameters = new ParameterDefinitionCollection( this );
		//noinspection ReturnOfCollectionOrArrayField
		return parameters;
	}

	public void setParameters( List<ParameterDefinition> parameters )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.parameters = parameters;
	}

	public ParameterDefinition getParameter( int index )
	{
		return getParameters().get( index );
	}

	@Override
	public IGenericParameterProvider getGenericParameterProviderType()
	{
		if( getDeclaringType() instanceof GenericInstanceType )
			return getDeclaringType().getElementsType();

		return getDeclaringType();
	}

	@Override
	public IGenericParameterProvider getGenericParameterProviderMethod()
	{
		return this;
	}

	@Override
	public GenericParameterType getGenericParameterType()
	{
		return GenericParameterType.Method;
	}

	@Override
	public boolean hasGenericParameters()
	{
		return genericParameters != null && !genericParameters.isEmpty();
	}

	@Override
	public Collection<GenericParameter> getGenericParameters()
	{
		if( genericParameters == null )
			genericParameters = new GenericParameterCollection( this );
		//noinspection ReturnOfCollectionOrArrayField
		return genericParameters;
	}

	protected void setGenericParameters( List<GenericParameter> genericParameters )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.genericParameters = genericParameters;
	}

	@Override
	public GenericParameter getGenericParameter( int index )
	{
		return genericParameters.get( index );
	}

	@Override
	public TypeReference getReturnType()
	{
		if( returnType != null )
			return returnType.getReturnType();
		return null;
	}

	@Override
	public void setReturnType( TypeReference type )
	{
		if( returnType != null )
			returnType.setReturnType( type );
	}

	@Override
	public MethodReturnType getMethodReturnType()
	{
		return returnType;
	}

	@SuppressWarnings( "unused" )
	public void setMethodReturnType( MethodReturnType methodReturnType )
	{
		returnType = methodReturnType;
	}

	@Override
	public String getFullName()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( getReturnType().getFullName() );
		sb.append( ' ' ).append( getMemberFullName() );
		Utils.getMethodSignatureFullName( this, sb );
		return sb.toString();
	}

	@SuppressWarnings( "unused" )
	public boolean isGenericInstance()
	{
		return false;
	}

	@Override
	public boolean containsGenericParameter()
	{
		if( getReturnType().containsGenericParameter() || super.containsGenericParameter() )
			return true;

		for( ParameterDefinition parameter : getParameters() )
		{
			if( parameter.getParameterType().containsGenericParameter() )
				return true;
		}
		return false;
	}

	public MethodReference getElementsMethod()
	{
		return this;
	}

	@SuppressWarnings( "ClassReferencesSubclass" )
	public MethodDefinition resolve()
	{
		if( !getModule().hasImage() )
			throw new UnsupportedOperationException();

		return getModule().resolve( this );
	}
}
