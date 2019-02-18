package mono.cecil;

public class PInvokeInfo
{
	public PInvokeInfo( int attributes, String entryPoint, ModuleReference module )
	{
		this.attributes = attributes;
		this.entryPoint = entryPoint;
		this.module = module;
	}

	private int attributes;
	private String entryPoint;
	private ModuleReference module;

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
	}

	public String getEntryPoint()
	{
		return entryPoint;
	}

	public void setEntryPoint( String entryPoint )
	{
		this.entryPoint = entryPoint;
	}

	public ModuleReference getModule()
	{
		return module;
	}

	public void setModule( ModuleReference module )
	{
		this.module = module;
	}

	public boolean isNoMangle()
	{
		return PInvokeAttributes.NoMangle.isSet( attributes );
	}

	public void setNoMangle( boolean value )
	{
		attributes = PInvokeAttributes.NoMangle.set( value, attributes );
	}

	public boolean isCharSetNotSpec()
	{
		return PInvokeAttributes.CharSetNotSpec.isSet( attributes );
	}

	public void setCharSetNotSpec( boolean value )
	{
		attributes = PInvokeAttributes.CharSetNotSpec.set( value, attributes );
	}

	public boolean isCharSetAnsi()
	{
		return PInvokeAttributes.CharSetAnsi.isSet( attributes );
	}

	public void setCharSetAnsi( boolean value )
	{
		attributes = PInvokeAttributes.CharSetAnsi.set( value, attributes );
	}

	public boolean isCharSetUnicode()
	{
		return PInvokeAttributes.CharSetUnicode.isSet( attributes );
	}

	public void setCharSetUnicode( boolean value )
	{
		attributes = PInvokeAttributes.CharSetUnicode.set( value, attributes );
	}

	public boolean isCharSetAuto()
	{
		return PInvokeAttributes.CharSetAuto.isSet( attributes );
	}

	public void setCharSetAuto( boolean value )
	{
		attributes = PInvokeAttributes.CharSetAuto.set( value, attributes );
	}

	public boolean isSupportsLastError()
	{
		return PInvokeAttributes.SupportsLastError.isSet( attributes );
	}

	public void setSupportsLastError( boolean value )
	{
		attributes = PInvokeAttributes.SupportsLastError.set( value, attributes );
	}

	public boolean isCallConvWinapi()
	{
		return PInvokeAttributes.CallConvWinapi.isSet( attributes );
	}

	public void setCallConvWinapi( boolean value )
	{
		attributes = PInvokeAttributes.CallConvWinapi.set( value, attributes );
	}

	public boolean isCallConvCdecl()
	{
		return PInvokeAttributes.CallConvCdecl.isSet( attributes );
	}

	public void setCallConvCdecl( boolean value )
	{
		attributes = PInvokeAttributes.CallConvCdecl.set( value, attributes );
	}

	public boolean isCallConvStdCall()
	{
		return PInvokeAttributes.CallConvStdCall.isSet( attributes );
	}

	public void setCallConvStdCall( boolean value )
	{
		attributes = PInvokeAttributes.CallConvStdCall.set( value, attributes );
	}

	public boolean isCallConvThiscall()
	{
		return PInvokeAttributes.CallConvThiscall.isSet( attributes );
	}

	public void setCallConvThiscall( boolean value )
	{
		attributes = PInvokeAttributes.CallConvThiscall.set( value, attributes );
	}

	public boolean isCallConvFastcall()
	{
		return PInvokeAttributes.CallConvFastcall.isSet( attributes );
	}

	public void setCallConvFastcall( boolean value )
	{
		attributes = PInvokeAttributes.CallConvFastcall.set( value, attributes );
	}

	public boolean isBestFitEnabled()
	{
		return PInvokeAttributes.BestFitEnabled.isSet( attributes );
	}

	public void setBestFitEnabled( boolean value )
	{
		attributes = PInvokeAttributes.BestFitEnabled.set( value, attributes );
	}

	public boolean isBestFitDisabled()
	{
		return PInvokeAttributes.BestFitDisabled.isSet( attributes );
	}

	public void setBestFitDisabled( boolean value )
	{
		attributes = PInvokeAttributes.BestFitDisabled.set( value, attributes );
	}

	public boolean isThrowOnUnmappableCharEnabled()
	{
		return PInvokeAttributes.ThrowOnUnmappableCharEnabled.isSet( attributes );
	}

	public void setThrowOnUnmappableCharEnabled( boolean value )
	{
		attributes = PInvokeAttributes.ThrowOnUnmappableCharEnabled.set( value, attributes );
	}

	public boolean isThrowOnUnmappableCharDisabled()
	{
		return PInvokeAttributes.ThrowOnUnmappableCharDisabled.isSet( attributes );
	}

	public void setThrowOnUnmappableCharDisabled( boolean value )
	{
		attributes = PInvokeAttributes.ThrowOnUnmappableCharDisabled.set( value, attributes );
	}
}
