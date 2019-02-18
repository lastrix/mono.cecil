package mono.cecil;

import mono.cecil.metadata.MetadataToken;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings( {"unused", "WeakerAccess"} )
public class MethodDefinition extends MethodReference implements IMemberDefinition, ISecurityDeclarationProvider
{
	public MethodDefinition()
	{
		setMetadataToken( new MetadataToken( TokenType.Method ) );
	}

	public MethodDefinition( String name, int attributes, TypeReference returnType )
	{
		super( name, returnType );
		this.attributes = attributes;
		//noinspection OverridableMethodCallDuringObjectConstruction
		setHasThis( !isStatic() );
		setMetadataToken( new MetadataToken( TokenType.Method ) );
	}

	private int attributes;
	private int implAttributes;
	private boolean semAttrsReady;
	private int semAttrs;
	private Collection<CustomAttribute> customAttributes;
	private Collection<SecurityDeclaration> securityDeclarations;
	private int rva;
	private PInvokeInfo pInvoke;
	private Collection<MethodReference> overrides;
	//private Object methodBody;

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
	}

	public int getImplAttributes()
	{
		return implAttributes;
	}

	public void setImplAttributes( int implAttributes )
	{
		this.implAttributes = implAttributes;
	}

	public int getSemAttrs()
	{
		if( semAttrsReady )
			return semAttrs;

		if( hasImage() )
		{
			readSemantics();
			return semAttrs;
		}

		semAttrs = MethodSemanticsAttributes.None.getMask();
		semAttrsReady = true;
		return semAttrs;
	}

	private void readSemantics()
	{
		if( semAttrsReady )
			return;

		if( !getModule().hasImage() )
			return;

		semAttrs = getModule().read( this, MetadataReader :: readAllSemantics );
	}

	public void setSemAttrs( int semAttrs )
	{
		this.semAttrs = semAttrs;
	}

	public boolean isSemAttrsReady()
	{
		return semAttrsReady;
	}

	public void setSemAttrsReady( boolean semAttrsReady )
	{
		this.semAttrsReady = semAttrsReady;
	}

	@Override
	public boolean hasSecurityDeclarations()
	{
		if( securityDeclarations != null )
			return !securityDeclarations.isEmpty();
		return Utils.hasSecurityDeclarations( this, getModule() );
	}

	@SuppressWarnings( "ReturnOfCollectionOrArrayField" )
	@Override
	public Collection<SecurityDeclaration> getSecurityDeclarations()
	{
		if( securityDeclarations != null )
			return securityDeclarations;

		//noinspection NestedAssignment
		return securityDeclarations = Utils.getSecurityDeclarations( this, getModule() );
	}

	@Override
	public boolean hasCustomAttributes()
	{
		if( customAttributes != null )
			return !customAttributes.isEmpty();
		return Utils.hasCustomAttributes( this, getModule() );
	}

	@Override
	public Collection<CustomAttribute> getCustomAttributes()
	{
		if( customAttributes != null )
			//noinspection ReturnOfCollectionOrArrayField
			return customAttributes;
		//noinspection NestedAssignment
		return customAttributes = Utils.getCustomAttributes( this, getModule() );
	}

	public int getRva()
	{
		return rva;
	}

	public void setRva( int rva )
	{
		this.rva = rva;
	}

//	public boolean hasBody()
//	{
//		// оно вообще нужно?
//		/*
//				return (attributes & (ushort) MethodAttributes.Abstract) == 0 &&
//					(attributes & (ushort) MethodAttributes.PInvokeImpl) == 0 &&
//					(impl_attributes & (ushort) MethodImplAttributes.InternalCall) == 0 &&
//					(impl_attributes & (ushort) MethodImplAttributes.Native) == 0 &&
//					(impl_attributes & (ushort) MethodImplAttributes.Unmanaged) == 0 &&
//					(impl_attributes & (ushort) MethodImplAttributes.Runtime) == 0;
//		 */
//		return false;
//	}

//	public Object getBody()
//	{
//		//  оно вообще нужно?
///*
//				MethodBody localBody = this.body;
//				if (localBody != null)
//					return localBody;
//
//				if (!HasBody)
//					return null;
//
//				if (HasImage && rva != 0)
//					return Module.Read (ref body, this, (method, reader) => reader.ReadMethodBody (method));
//
//				return body = new MethodBody (this);
//
// */
//		return methodBody;
//	}

//	public void setBody( Object methodBody )
//	{
//		// оно вообще нужно?
///*
//				var module = this.Module;
//				if (module == null) {
//					body = value;
//					return;
//				}
//
//				// we reset Body to null in ILSpy to save memory; so we need that operation to be thread-safe
//				lock (module.SyncRoot) {
//					body = value;
//				}
//
//		 */
//		this.methodBody = methodBody;
//	}

	public boolean hasPInvokeInfo()
	{
		return pInvoke != null || isPInvokeImpl();

	}

	public PInvokeInfo getPInvoke()
	{
		if( pInvoke != null )
			return pInvoke;

		if( hasImage() && isPInvokeImpl() )
			//noinspection NestedAssignment
			return pInvoke = getModule().read( this, MetadataReader :: readPInvokeInfo );
		return pInvoke;
	}

	public void setPInvoke( PInvokeInfo pInvoke )
	{
		setPInvokeImpl( true );
		this.pInvoke = pInvoke;
	}

	public boolean hasOverrides()
	{
		if( overrides != null )
			return !overrides.isEmpty();

		if( hasImage() )
			return getModule().read( this, MetadataReader :: hasOverrides );

		return false;
	}

	@SuppressWarnings( "NestedAssignment" )
	public Collection<MethodReference> getOverrides()
	{
		if( overrides != null )
			//noinspection ReturnOfCollectionOrArrayField
			return overrides;

		if( hasImage() )
			return overrides = getModule().read( this, MetadataReader :: readOverrides );

		return overrides = Collections.emptyList();
	}

	@Override
	public boolean hasGenericParameters()
	{
		if( genericParameters != null )
			return genericParameters.isEmpty();

		return Utils.hasGenericParameters( this, getModule() );
	}

	@Override
	public Collection<GenericParameter> getGenericParameters()
	{
		if( genericParameters != null )
			return genericParameters;

		setGenericParameters( Utils.getGenericParameters( this, getModule() ) );
		return genericParameters;
	}

	public boolean isCompilerControlled()
	{
		return MethodAttributes.CompilerControlled.isSet( getAttributes() );
	}

	public void setCompilerControlled( boolean value )
	{
		attributes = MethodAttributes.CompilerControlled.set( value, getAttributes() );
	}

	public boolean isPrivate()
	{
		return MethodAttributes.Private.isSet( getAttributes() );
	}

	public void setPrivate( boolean value )
	{
		attributes = MethodAttributes.Private.set( value, getAttributes() );
	}

	public boolean isFamilyAndAssembly()
	{
		return MethodAttributes.FamANDAssem.isSet( getAttributes() );
	}

	public void setFamilyAndAssembly( boolean value )
	{
		attributes = MethodAttributes.FamANDAssem.set( value, getAttributes() );
	}

	public boolean isAssembly()
	{
		return MethodAttributes.Assembly.isSet( getAttributes() );
	}

	public void setAssembly( boolean value )
	{
		attributes = MethodAttributes.Assembly.set( value, getAttributes() );
	}

	public boolean isFamily()
	{
		return MethodAttributes.Family.isSet( getAttributes() );
	}

	public void setFamily( boolean value )
	{
		attributes = MethodAttributes.Family.set( value, getAttributes() );
	}

	public boolean isFamilyOrAssembly()
	{
		return MethodAttributes.FamORAssem.isSet( getAttributes() );
	}

	public void setFamilyOrAssembly( boolean value )
	{
		attributes = MethodAttributes.FamORAssem.set( value, getAttributes() );
	}

	public boolean isPublic()
	{
		return MethodAttributes.Public.isSet( getAttributes() );
	}

	public void setPublic( boolean value )
	{
		attributes = MethodAttributes.Public.set( value, getAttributes() );
	}

	public boolean isStatic()
	{
		return MethodAttributes.Static.isSet( getAttributes() );
	}

	public void setStatic( boolean value )
	{
		attributes = MethodAttributes.Static.set( value, getAttributes() );
	}

	public boolean isFinal()
	{
		return MethodAttributes.Final.isSet( getAttributes() );
	}

	public void setFinal( boolean value )
	{
		attributes = MethodAttributes.Final.set( value, getAttributes() );
	}

	public boolean isVirtual()
	{
		return MethodAttributes.Virtual.isSet( getAttributes() );
	}

	public void setVirtual( boolean value )
	{
		attributes = MethodAttributes.Virtual.set( value, getAttributes() );
	}

	public boolean isHideBySig()
	{
		return MethodAttributes.HideBySig.isSet( getAttributes() );
	}

	public void setHideBySig( boolean value )
	{
		attributes = MethodAttributes.HideBySig.set( value, getAttributes() );
	}

	public boolean isReuseSlot()
	{
		return MethodAttributes.ReuseSlot.isSet( getAttributes() );
	}

	public void setReuseSlot( boolean value )
	{
		attributes = MethodAttributes.ReuseSlot.set( value, getAttributes() );
	}

	public boolean isNewSlot()
	{
		return MethodAttributes.NewSlot.isSet( getAttributes() );
	}

	public void setNewSlot( boolean value )
	{
		attributes = MethodAttributes.NewSlot.set( value, getAttributes() );
	}

	public boolean isCheckAccessOnOverride()
	{
		return MethodAttributes.CheckAccessOnOverride.isSet( getAttributes() );
	}

	public void setCheckAccessOnOverride( boolean value )
	{
		attributes = MethodAttributes.CheckAccessOnOverride.set( value, getAttributes() );
	}

	public boolean isAbstract()
	{
		return MethodAttributes.Abstract.isSet( getAttributes() );
	}

	public void setAbstract( boolean value )
	{
		attributes = MethodAttributes.Abstract.set( value, getAttributes() );
	}

	@Override
	public boolean isSpecialName()
	{
		return MethodAttributes.SpecialName.isSet( getAttributes() );
	}

	@Override
	public void setSpecialName( boolean value )
	{
		attributes = MethodAttributes.SpecialName.set( value, getAttributes() );
	}

	public boolean isPInvokeImpl()
	{
		return MethodAttributes.PInvokeImpl.isSet( getAttributes() );
	}

	public void setPInvokeImpl( boolean value )
	{
		attributes = MethodAttributes.PInvokeImpl.set( value, getAttributes() );
	}

	public boolean isUnmanagedExport()
	{
		return MethodAttributes.UnmanagedExport.isSet( getAttributes() );
	}

	public void setUnmanagedExport( boolean value )
	{
		attributes = MethodAttributes.UnmanagedExport.set( value, getAttributes() );
	}

	@Override
	public boolean isRuntimeSpecialName()
	{
		return MethodAttributes.RTSpecialName.isSet( getAttributes() );
	}

	@Override
	public void setRuntimeSpecialName( boolean value )
	{
		attributes = MethodAttributes.RTSpecialName.set( value, getAttributes() );
	}

	public boolean isHasSecurity()
	{
		return MethodAttributes.HasSecurity.isSet( getAttributes() );
	}

	public void setHasSecurity( boolean value )
	{
		attributes = MethodAttributes.HasSecurity.set( value, getAttributes() );
	}

	////

	public boolean isIL()
	{
		return MethodImplAttributes.IL.isSet( getImplAttributes() );
	}

	public void setIL( boolean value )
	{
		implAttributes = MethodImplAttributes.IL.set( value, getImplAttributes() );
	}

	public boolean isNative()
	{
		return MethodImplAttributes.Native.isSet( getImplAttributes() );
	}

	public void setNative( boolean value )
	{
		implAttributes = MethodImplAttributes.Native.set( value, getImplAttributes() );
	}

	public boolean isRuntime()
	{
		return MethodImplAttributes.Runtime.isSet( getImplAttributes() );
	}

	public void setRuntime( boolean value )
	{
		implAttributes = MethodImplAttributes.Runtime.set( value, getImplAttributes() );
	}

	public boolean isUnmanaged()
	{
		return MethodImplAttributes.Unmanaged.isSet( getImplAttributes() );
	}

	public void setUnmanaged( boolean value )
	{
		implAttributes = MethodImplAttributes.Unmanaged.set( value, getImplAttributes() );
	}

	public boolean isManaged()
	{
		return MethodImplAttributes.Managed.isSet( getImplAttributes() );
	}

	public void setManaged( boolean value )
	{
		implAttributes = MethodImplAttributes.Managed.set( value, getImplAttributes() );
	}

	public boolean isForwardRef()
	{
		return MethodImplAttributes.ForwardRef.isSet( getImplAttributes() );
	}

	public void setForwardRef( boolean value )
	{
		implAttributes = MethodImplAttributes.ForwardRef.set( value, getImplAttributes() );
	}

	public boolean isPreserveSig()
	{
		return MethodImplAttributes.PreserveSig.isSet( getImplAttributes() );
	}

	public void setPreserveSig( boolean value )
	{
		implAttributes = MethodImplAttributes.PreserveSig.set( value, getImplAttributes() );
	}

	public boolean isInternalCall()
	{
		return MethodImplAttributes.InternalCall.isSet( getImplAttributes() );
	}

	public void setInternalCall( boolean value )
	{
		implAttributes = MethodImplAttributes.InternalCall.set( value, getImplAttributes() );
	}

	public boolean isSynchronized()
	{
		return MethodImplAttributes.Synchronized.isSet( getImplAttributes() );
	}

	public void setSynchronized( boolean value )
	{
		implAttributes = MethodImplAttributes.Synchronized.set( value, getImplAttributes() );
	}

	public boolean isNoInlining()
	{
		return MethodImplAttributes.NoInlining.isSet( getImplAttributes() );
	}

	public void setNoInlining( boolean value )
	{
		implAttributes = MethodImplAttributes.NoInlining.set( value, getImplAttributes() );
	}

	public boolean isNoOptimization()
	{
		return MethodImplAttributes.NoOptimization.isSet( getImplAttributes() );
	}

	public void setNoOptimization( boolean value )
	{
		implAttributes = MethodImplAttributes.NoOptimization.set( value, getImplAttributes() );
	}

	////

	public boolean isSetter()
	{
		return MethodSemanticsAttributes.Setter.isSet( getSemAttrs() );
	}

	public void setSetter( boolean value )
	{
		semAttrs = MethodSemanticsAttributes.Setter.set( value, getSemAttrs() );
	}

	public boolean isGetter()
	{
		return MethodSemanticsAttributes.Getter.isSet( getSemAttrs() );
	}

	public void setGetter( boolean value )
	{
		semAttrs = MethodSemanticsAttributes.Getter.set( value, getSemAttrs() );
	}

	public boolean isOther()
	{
		return MethodSemanticsAttributes.Other.isSet( getSemAttrs() );
	}

	public void setOther( boolean value )
	{
		semAttrs = MethodSemanticsAttributes.Other.set( value, getSemAttrs() );
	}

	public boolean isAddOn()
	{
		return MethodSemanticsAttributes.AddOn.isSet( getSemAttrs() );
	}

	public void setAddOn( boolean value )
	{
		semAttrs = MethodSemanticsAttributes.AddOn.set( value, getSemAttrs() );
	}

	public boolean isRemoveOn()
	{
		return MethodSemanticsAttributes.RemoveOn.isSet( getSemAttrs() );
	}

	public void setRemoveOn( boolean value )
	{
		semAttrs = MethodSemanticsAttributes.RemoveOn.set( value, getSemAttrs() );
	}

	public boolean isFire()
	{
		return MethodSemanticsAttributes.Fire.isSet( getSemAttrs() );
	}

	public void setFire( boolean value )
	{
		semAttrs = MethodSemanticsAttributes.Fire.set( value, getSemAttrs() );
	}

	@Override
	public TypeDefinition getDeclaringType()
	{
		return (TypeDefinition)super.getDeclaringType();
	}

	@Override
	public void setDeclaringType( TypeDefinition type )
	{
		super.setDeclaringType( type );
	}

	public boolean isConstructor()
	{
		return isRuntimeSpecialName() && isSpecialName() && ( getName().equals( ".cctor" ) || getName().equals( ".ctor" ) );
	}

	@Override
	public boolean isDefinition()
	{
		return true;
	}

	@Override
	public MethodDefinition resolve()
	{
		return this;
	}
}
