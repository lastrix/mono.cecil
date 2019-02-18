package mono.cecil;

import java.util.ArrayList;
import java.util.Collection;

public class GenericInstanceMethod extends MethodSpecification implements IGenericInstance
{
	public GenericInstanceMethod( MethodReference method )
	{
		super( method );
	}

	private Collection<TypeReference> genericArguments;

	@Override
	public boolean hasGenericArguments()
	{
		return genericArguments != null && !genericArguments.isEmpty();
	}

	@Override
	public Collection<TypeReference> getGenericArguments()
	{
		if( genericArguments == null )
			genericArguments = new ArrayList<>();
		//noinspection ReturnOfCollectionOrArrayField
		return genericArguments;
	}

	@Override
	public boolean isGenericInstance()
	{
		return true;
	}

	@Override
	public IGenericParameterProvider getGenericParameterProviderType()
	{
		return getElementMethod().getDeclaringType();
	}

	@Override
	public IGenericParameterProvider getGenericParameterProviderMethod()
	{
		return getElementMethod();
	}

	@Override
	public boolean containsGenericParameter()
	{
		return Utils.containsGenericParameter( this ) || super.containsGenericParameter();
	}

	@Override
	public String getFullName()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( getElementMethod().getReturnType().getFullName() )
				.append( ' ' )
				.append( getElementMethod().getDeclaringType().getFullName() )
				.append( "::" )
				.append( getElementMethod().getName() );
		Utils.getGenericInstanceFullName( this, sb );
		Utils.getMethodSignatureFullName( this, sb );
		return sb.toString();
	}
}
