package mono.cecil;

@SuppressWarnings( "ClassReferencesSubclass" )
public abstract class EventReference extends MemberReference
{
	protected EventReference( String name, TypeReference eventType )
	{
		super( name );
		if( eventType == null )
			throw new IllegalArgumentException();
		this.eventType = eventType;
	}

	private TypeReference eventType;

	public TypeReference getEventType()
	{
		return eventType;
	}

	public void setEventType( TypeReference eventType )
	{
		this.eventType = eventType;
	}

	@Override
	public String getFullName()
	{
		return eventType.getFullName() + ' ' + getMemberFullName();
	}

	public abstract EventDefinition resolve();
}
