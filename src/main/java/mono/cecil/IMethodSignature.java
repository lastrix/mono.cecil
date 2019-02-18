package mono.cecil;

import java.util.Collection;

public interface IMethodSignature extends IMetadataTokenProvider
{
	boolean getHasThis();

	void setHasThis( boolean value );

	boolean isExplicitThis();

	void setExplicitThis( boolean value );

	MethodCallingConvention getMethodCallingConvention();

	void setMethodCallingConvention( MethodCallingConvention value );

	boolean hasParameters();

	Collection<ParameterDefinition> getParameters();

	TypeReference getReturnType();

	void setReturnType( TypeReference type );

	MethodReturnType getMethodReturnType();
}
