package mono.cecil;

import java.util.Collection;


@SuppressWarnings( {"AssignmentToCollectionOrArrayFieldFromParameter", "ReturnOfCollectionOrArrayField", "NestedAssignment", "unused"} )
public class SecurityDeclaration
{
	public SecurityDeclaration( SecurityAction action, int signature, ModuleDefinition module )
	{
		this.action = action;
		this.signature = signature;
		this.module = module;
		resolved = false;
	}

	public SecurityDeclaration( SecurityAction action )
	{
		this.action = action;
		resolved = true;
	}

	public SecurityDeclaration( SecurityAction action, byte[] blob )
	{
		this.action = action;
		this.blob = blob;
		resolved = false;
	}

	private int signature;
	private byte[] blob;
	private ModuleDefinition module;

	private boolean resolved;
	private SecurityAction action;
	private Collection<SecurityAttribute> securityAttributes;

	public int getSignature()
	{
		return signature;
	}

	public SecurityAction getAction()
	{
		return action;
	}

	public void setAction( SecurityAction action )
	{
		this.action = action;
	}

	public boolean hasSecurityAttributes()
	{
		resolve();
		return !securityAttributes.isEmpty();
	}

	public Collection<SecurityAttribute> getSecurityAttributes()
	{
		resolve();
		return securityAttributes;
	}

	public void setSecurityAttributes( Collection<SecurityAttribute> securityAttributes )
	{
		this.securityAttributes = securityAttributes;
	}

	public boolean hasImage()
	{
		return module != null && module.hasImage();
	}

	public byte[] getBlob()
	{
		if( blob != null )
			return blob;

		if( !hasImage() || signature == 0 )
			throw new UnsupportedOperationException();

		return blob = module.read( this, ( reader, item ) -> reader.readSecurityDeclarationBlob( item.getSignature() ) );
	}

	private void resolve()
	{
		if( resolved || !hasImage() )
			return;

		module.read( this, ( reader, item ) -> {
			reader.readSecurityDeclarationSignature( item );
			return null;
		} );
		resolved = true;
	}
}
