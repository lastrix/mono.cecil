package mono.cecil;

public interface Action<T1, T2>
{
	void invoke( T1 t1, T2 t2 );
}
