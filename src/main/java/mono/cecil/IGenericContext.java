package mono.cecil;

public interface IGenericContext
{
	boolean isDefinition();

	IGenericParameterProvider getGenericParameterProviderType();

	IGenericParameterProvider getGenericParameterProviderMethod();
}
