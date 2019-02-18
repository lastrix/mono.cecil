package mono.cecil;

public interface Callback<R, T>
{
	R invoke( MetadataReader reader, T item );
}
