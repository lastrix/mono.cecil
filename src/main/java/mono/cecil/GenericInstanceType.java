package mono.cecil;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings( {"ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"} )
public class GenericInstanceType extends TypeSpecification implements IGenericInstance
{
	public GenericInstanceType( TypeReference type )
	{
		super( type );
		setValueType( type.isValueType() );
		setEtype( ElementType.GenericInst );
	}

	public GenericInstanceType( TypeReference type, Collection<TypeReference> genericArguments )
	{
		this( type );
		this.genericArguments = genericArguments;
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
		return genericArguments;
	}

	public void setGenericArguments( Collection<TypeReference> genericArguments )
	{
		this.genericArguments = genericArguments;
	}

	@Override
	public void setDeclaringType( TypeReference declaringType )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public TypeReference getDeclaringType()
	{
		return getElementType().getDeclaringType();
	}

	@Override
	public String getFullName()
	{
		StringBuilder sb = new StringBuilder();
		String fullName = super.getFullName();
		sb.append( fullName.substring( 0, fullName.lastIndexOf( '`' ) ) );
		Utils.getGenericInstanceFullName( this, sb );
		return sb.toString();
	}

	@Override
	public boolean isGenericInstance()
	{
		return true;
	}

	@Override
	public boolean containsGenericParameter()
	{
		return Utils.containsGenericParameter( this ) || super.containsGenericParameter();
	}

	@Override
	public IGenericParameterProvider getGenericParameterProviderType()
	{
		return getElementType();
	}
}
