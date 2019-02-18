package mono.cecil.pe;

import mono.cecil.Utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;

@SuppressWarnings( "WeakerAccess" )
public class ByteBuffer
{
	private byte[] buffer;
	private int position;

	public ByteBuffer( byte[] buffer )
	{
		reset( buffer );
	}

	public ByteBuffer( int length )
	{
		buffer = new byte[length];
	}

	public byte[] bytes()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return buffer;
	}

	public final void reset( byte[] buffer )
	{
		this.buffer = buffer == null ? Utils.EMPTY_BYTE_ARRAY : buffer;
		position = 0;
	}

	public int position()
	{
		return position;
	}

	public void advance( int length )
	{
		position += length;
	}

	public void offset( int position )
	{
		this.position = position;
	}

	public int readByte()
	{
		//noinspection ValueOfIncrementOrDecrementUsed
		return buffer[position++];
	}

	public byte[] readBytes( int length )
	{
		byte[] bytes = new byte[length];
		read( bytes, 0, length );
		return bytes;
	}

	public void read( byte[] dest, int offset, int length )
	{
		if( buffer.length < position + length )
			throw new BadImageFormatException();
		System.arraycopy( buffer, position, dest, offset, length );
		position += length;
	}

	public int readUInt16()
	{
		int b1 = readByte();
		int b2 = readByte();
		//noinspection MagicNumber
		return ( b2 << 8 ) & 0x0000FF00 | b1 & 0x000000FF;
	}

	public int readInt16()
	{
		return readUInt16();
	}

	public int readUInt32()
	{
		int b1 = readByte();
		int b2 = readByte();
		int b3 = readByte();
		int b4 = readByte();
		//noinspection MagicNumber
		return ( b4 << 24 ) & 0xFF000000 | ( b3 << 16 ) & 0x00FF0000 | ( b2 << 8 ) & 0x0000FF00 | b1 & 0x000000FF;
	}

	public int readInt32()
	{
		return readUInt32();
	}

	public long readUInt64()
	{
		long low = readUInt32();
		long high = readUInt32();
		//noinspection MagicNumber
		return ( high & 0xFFFFFFFFL ) << 32
				| ( low & 0xFFFFFFFFL );
	}

	public long readInt64()
	{
		return readUInt64();
	}

	public float readSingle()
	{
		return java.nio.ByteBuffer.wrap( readBytes( 4 ) ).order( ByteOrder.LITTLE_ENDIAN ).getFloat();
	}

	public double readDouble()
	{
		return java.nio.ByteBuffer.wrap( readBytes( 8 ) ).order( ByteOrder.LITTLE_ENDIAN ).getDouble();
	}

	@SuppressWarnings( "MagicNumber" )
	public int readCompressedUInt32()
	{
		if( ( buffer[position] & 0x80 ) == 0 )
		{
			position++;
			return buffer[position - 1];
		}

		if( ( buffer[position] & 0x40 ) == 0 )
		{
			position += 2;
			return ( buffer[position - 2] & 0x0000007F ) << 8 | ( buffer[position - 1] & 0x000000FF );
		}

		position += 4;
		return ( buffer[position - 4] & 0x0000003F ) << 24
				| ( buffer[position - 3] & 0x000000FF ) << 16
				| ( buffer[position - 2] & 0x000000FF ) << 8
				| ( buffer[position - 1] & 0x000000FF );
	}

	@SuppressWarnings( "MagicNumber" )
	public int readCompressedInt32()
	{
		int value = readCompressedUInt32() >> 1;
		if( ( value & 1 ) == 0 )
			return value;
		if( value < 0x40 )
			return value - 0x40;
		if( value < 0x2000 )
			return value - 0x2000;
		if( value < 0x10000000 )
			return value - 0x10000000;
		return value - 0x20000000;
	}

	public String readAlignedString( int size )
	{
		int read = 0;
		StringBuilder sb = new StringBuilder();
		while( read < size )
		{
			//noinspection NumericCastThatLosesPrecision
			char c = (char)readByte();
			if( c == 0 )
				break;
			sb.append( c );
			read++;
		}

		int amount = -1 + ( ( read + 4 ) & ~3 ) - read;
		advance( amount );

		return sb.toString();
	}

	public String readCString( int size )
	{
		StringBuilder sb = new StringBuilder();
		boolean dry = false;
		for( int i = 0; i < size; i++ )
		{
			//noinspection NumericCastThatLosesPrecision
			char c = (char)readByte();
			if( c == 0 )
				dry = true;

			if( !dry )
				sb.append( c );
		}
		return sb.toString();
	}

	public String readUTF8String()
	{
		int start = position;
		while( position < buffer.length )
			//noinspection ValueOfIncrementOrDecrementUsed
			if( buffer[position++] == 0 )
				break;

		byte[] data = new byte[position - start - 1];
		System.arraycopy( buffer, start, data, 0, position - start - 1 );
		try
		{
			return new String( data, "UTF-8" );
		} catch( UnsupportedEncodingException e )
		{
			throw new IllegalStateException( "Unable to locate UTF-8 encoding", e );
		}
	}

	public DataDirectory readDataDirectory()
	{
		return new DataDirectory( readUInt32(), readUInt32() );
	}

	public int length()
	{
		return buffer.length;
	}

	/*
		public float ReadSingle ()
		{
			if (!BitConverter.IsLittleEndian) {
				var bytes = ReadBytes (4);
				Array.Reverse (bytes);
				return BitConverter.ToSingle (bytes, 0);
			}

			float value = BitConverter.ToSingle (buffer, position);
			position += 4;
			return value;
		}

		public double ReadDouble ()
		{
			if (!BitConverter.IsLittleEndian) {
				var bytes = ReadBytes (8);
				Array.Reverse (bytes);
				return BitConverter.ToDouble (bytes, 0);
			}

			double value = BitConverter.ToDouble (buffer, position);
			position += 8;
			return value;
		}
	 */
}
