package mono.cecil.pe;

import mono.cecil.*;
import mono.cecil.metadata.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings( "MagicNumber" )
public class ImageReader extends ByteBuffer
{
	private ImageReader( String fileName, byte[] buffer )
	{
		super( buffer );
		image = new Image();
		image.setFileName( fileName );
		image.setHash( createHash( buffer ) );
	}

	private static String createHash( byte[] buffer )
	{
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance( "SHA-256" );
		} catch( NoSuchAlgorithmException e )
		{
			throw new IllegalStateException( e );
		}
		return new BigInteger( md.digest( buffer ) ).toString( 16 );
	}

	private final Image image;

	// ******************************** Attributes ****************************************************************** //
	public Image getImage()
	{
		return image;
	}

	private DataDirectory cli;
	private DataDirectory metadata;

	// ******************************** Operations ****************************************************************** //
	private void moveTo( DataDirectory directory )
	{
		offset( image.resolveVirtualAddress( directory.getVirtualAddress() ) );
	}

	private void readImage()
	{
		offset( 0 );

		if( length() < 128 )
			throw new BadImageFormatException();

		// - DOSHeader

		// PE					2
		// Start				58
		// Lfanew				4
		// End					64

		if( readUInt16() != 0x5a4d )
			throw new BadImageFormatException();

		advance( 58 );
		offset( readUInt32() );

		if( readUInt32() != 0x00004550 )
			throw new BadImageFormatException();

		// - PEFileHeader

		// Machine				2
		image.setArchitecture( TargetArchitecture.getByCode( readUInt16() ) );

		// NumberOfSections		2
		int sections = readUInt16();

		// TimeDateStamp		4
		// PointerToSymbolTable	4
		// NumberOfSymbols		4
		// OptionalHeaderSize	2
		advance( 14 );

		// Characteristics		2
		int characteristics = readUInt16();

		readOptionalHeaders( characteristics );
		readSections( sections );
		readCliHeader();
		readMetadata();
	}

	private void readOptionalHeaders( int characteristics )
	{
		// - PEOptionalHeader
		//   - StandardFieldsHeader

		// Magic				2
		boolean pe64 = readUInt16() == 0x20b;
		//						pe32 || pe64

		// LMajor				1
		// LMinor				1
		// CodeSize				4
		// InitializedDataSize	4
		// UninitializedDataSize4
		// EntryPointRVA		4
		// BaseOfCode			4
		// BaseOfData			4 || 0

		//   - NTSpecificFieldsHeader

		// ImageBase			4 || 8
		// SectionAlignment		4
		// FileAlignement		4
		// OSMajor				2
		// OSMinor				2
		// UserMajor			2
		// UserMinor			2
		// SubSysMajor			2
		// SubSysMinor			2
		// Reserved				4
		// ImageSize			4
		// HeaderSize			4
		// FileChecksum			4
		advance( 66 );

		// SubSystem			2
		int subsystem = readUInt16();
		updateModuleKind( characteristics, subsystem );

		// DLLFlags				2
		image.setCharacteristics( readUInt16() );

		// StackReserveSize		4 || 8
		// StackCommitSize		4 || 8
		// HeapReserveSize		4 || 8
		// HeapCommitSize		4 || 8
		// LoaderFlags			4
		// NumberOfDataDir		4

		//   - DataDirectoriesHeader

		// ExportTable			8
		// ImportTable			8
		// ResourceTable		8
		// ExceptionTable		8
		// CertificateTable		8
		// BaseRelocationTable	8

		advance( pe64 ? 88 : 72 );

		// Debug				8
		image.setDebug( readDataDirectory() );

		// Copyright			8
		// GlobalPtr			8
		// TLSTable				8
		// LoadConfigTable		8
		// BoundImport			8
		// IAT					8
		// DelayImportDescriptor8
		advance( 56 );

		// CLIHeader			8
		cli = readDataDirectory();

		if( cli.isZero() )
			throw new BadImageFormatException();

		// Reserved				8
		advance( 8 );
	}

	private void updateModuleKind( int characteristics, int subsystem )
	{
		if( ( characteristics & 0x2000 ) != 0 ) // ImageCharacteristics.Dll
			image.setKind( ModuleKind.Dll );

		if( subsystem == 0x2 || subsystem == 0x9 ) // SubSystem.WindowsGui || SubSystem.WindowsCeGui
			image.setKind( ModuleKind.Windows );

		image.setKind( ModuleKind.Console );
	}

	private void readSections( int sections )
	{
		while( sections > 0 )
		{
			sections--;
			readSection();
		}
	}

	private void readSection()
	{
		Section section = new Section();
		// Name
		section.name( readCString( 8 ) );

		// VirtualSize		4
		advance( 4 );

		// VirtualAddress	4
		section.virtualAddress( readUInt32() )
				// SizeOfRawData	4
				.sizeOfRawData( readUInt32() )
				// PointerToRawData	4
				.pointerToRawData( readUInt32() );

		// PointerToRelocations		4
		// PointerToLineNumbers		4
		// NumberOfRelocations		2
		// NumberOfLineNumbers		2
		// Characteristics			4
		advance( 16 );

		image.getSections().add( section );

		readSectionData( section );
	}

	private void readSectionData( Section section )
	{
		int currentPosition = position();

		offset( section.pointerToRawData() );
		byte[] data = new byte[section.sizeOfRawData()];
		read( data, 0, data.length );
		section.data( new ByteBuffer( data ) );

		offset( currentPosition );
	}

	private void readCliHeader()
	{
		moveTo( cli );

		// - CLIHeader

		// Cb						4
		// MajorRuntimeVersion		2
		// MinorRuntimeVersion		2
		advance( 8 );
		// Metadata					8
		metadata = readDataDirectory();
		// Flags					4
		image.setAttributes( readUInt32() );
		// EntryPointToken			4
		image.setEntryPointToken( readUInt32() );
		// Resources				8
		image.setResources( readDataDirectory() );
		// StrongNameSignature		8
		image.setStrongName( readDataDirectory() );
		// CodeManagerTable			8
		// VTableFixups				8
		// ExportAddressTableJumps	8
		// ManagedNativeHeader		8
	}

	private void readMetadata()
	{
		moveTo( metadata );

		if( readUInt32() != 0x424a5342 )
			throw new BadImageFormatException();

		// MajorVersion			2
		// MinorVersion			2
		// Reserved				4
		advance( 8 );

		image.setRuntimeVersion( readCString( readUInt32() ) );
		// Flags		2
		advance( 2 );

		int streams = readUInt16();
		Section section = image.getSectionAtVirtualAddress( metadata.getVirtualAddress() );
		if( section == null )
			throw new BadImageFormatException();

		image.setMetadataSection( section );

		while( streams > 0 )
		{
			streams--;
			readMetadataStream( section );
		}

		if( image.getTableHeap() != null )
			readTableHeap();
	}

	private void readMetadataStream( Section section )
	{
		// Offset		4
		int offset = metadata.getVirtualAddress() - section.virtualAddress() + readUInt32();

		// Size			4
		int size = readUInt32();
		String name = readAlignedString( 16 );

		switch( name )
		{
			case "#~":
			case "#-":
				image.setTableHeap( new TableHeap( section, offset, size ) );
				break;

			case "#Strings":
				image.setStringHeap( new StringHeap( section, offset, size ) );
				break;
			case "#Blob":
				image.setBlobHeap( new BlobHeap( section, offset, size ) );
				break;
			case "#GUID":
				image.setGuidHeap( new GuidHeap( section, offset, size ) );
				break;
			case "#US":
				image.setUserStringHeap( new UserStringHeap( section, offset, size ) );
				break;

			default:
				throw new BadImageFormatException( name );
		}
	}

	private void readTableHeap()
	{
		TableHeap heap = image.getTableHeap();
		int start = heap.getSection().pointerToRawData();
		offset( start + heap.getOffset() );

		// Reserved			4
		// MajorVersion		1
		// MinorVersion		1
		advance( 6 );

		// HeapSizes		1
		int sizes = readByte();

		// Reserved2		1
		advance( 1 );

		// Valid			8
		heap.setValid( readInt64() );

		// Sorted			8
		heap.setSorted( readInt64() );

		for( Table table : Table.values() )
		{
			if( !heap.hasTable( table ) )
				continue;

			heap.getTable( table ).setLength( readUInt32() );
		}

		setIndexSize( image.getStringHeap(), sizes, 0x1 );
		setIndexSize( image.getGuidHeap(), sizes, 0x2 );
		setIndexSize( image.getBlobHeap(), sizes, 0x4 );

		computeTableInformations();
	}

	private static void setIndexSize( Heap heap, int sizes, int flag )
	{
		if( heap == null )
			return;

		heap.setIndexSize( ( sizes & flag ) > 0 ? 4 : 2 );
	}

	private void computeTableInformations()
	{
		int offset = position() - image.getMetadataSection().pointerToRawData();

		int stridx_size = image.getStringHeap().getIndexSize();
		int blobidx_size = image.getBlobHeap() != null ? image.getBlobHeap().getIndexSize() : 2;

		TableHeap heap = image.getTableHeap();

		for( Table table : Table.values() )
		{
			if( !heap.hasTable( table ) )
				continue;

			int size;
			switch( table )
			{
				case Module:
					size = 2    // Generation
							+ stridx_size    // Name
							+ ( image.getGuidHeap().getIndexSize() * 3 );    // Mvid, EncId, EncBaseId
					break;
				case TypeRef:
					size = getCodedIndexSize( CodedIndex.ResolutionScope )    // ResolutionScope
							+ ( stridx_size * 2 );    // Name, Namespace
					break;
				case TypeDef:
					size = 4    // Flags
							+ ( stridx_size * 2 )    // Name, Namespace
							+ getCodedIndexSize( CodedIndex.TypeDefOrRef )    // BaseType
							+ getTableIndexSize( Table.Field )    // FieldList
							+ getTableIndexSize( Table.Method );    // MethodList
					break;
				case FieldPtr:
					size = getTableIndexSize( Table.Field );    // Field
					break;
				case Field:
					size = 2    // Flags
							+ stridx_size    // Name
							+ blobidx_size;    // Signature
					break;
				case MethodPtr:
					size = getTableIndexSize( Table.Method );    // Method
					break;
				case Method:
					size = 8    // Rva 4, ImplFlags 2, Flags 2
							+ stridx_size    // Name
							+ blobidx_size    // Signature
							+ getTableIndexSize( Table.Param ); // ParamList
					break;
				case ParamPtr:
					size = getTableIndexSize( Table.Param ); // Param
					break;
				case Param:
					size = 4    // Flags 2, Sequence 2
							+ stridx_size;    // Name
					break;
				case InterfaceImpl:
					size = getTableIndexSize( Table.TypeDef )    // Class
							+ getCodedIndexSize( CodedIndex.TypeDefOrRef );    // Interface
					break;
				case MemberRef:
					size = getCodedIndexSize( CodedIndex.MemberRefParent )    // Class
							+ stridx_size    // Name
							+ blobidx_size;    // Signature
					break;
				case Constant:
					size = 2    // Type
							+ getCodedIndexSize( CodedIndex.HasConstant )    // Parent
							+ blobidx_size;    // Value
					break;
				case CustomAttribute:
					size = getCodedIndexSize( CodedIndex.HasCustomAttribute )    // Parent
							+ getCodedIndexSize( CodedIndex.CustomAttributeType )    // Type
							+ blobidx_size;    // Value
					break;
				case FieldMarshal:
					size = getCodedIndexSize( CodedIndex.HasFieldMarshal )    // Parent
							+ blobidx_size;    // NativeType
					break;
				case DeclSecurity:
					size = 2    // Action
							+ getCodedIndexSize( CodedIndex.HasDeclSecurity )    // Parent
							+ blobidx_size;    // PermissionSet
					break;
				case ClassLayout:
					size = 6    // PackingSize 2, ClassSize 4
							+ getTableIndexSize( Table.TypeDef );    // Parent
					break;
				case FieldLayout:
					size = 4    // Offset
							+ getTableIndexSize( Table.Field );    // Field
					break;
				case StandAloneSig:
					size = blobidx_size;    // Signature
					break;
				case EventMap:
					size = getTableIndexSize( Table.TypeDef )    // Parent
							+ getTableIndexSize( Table.Event );    // EventList
					break;
				case EventPtr:
					size = getTableIndexSize( Table.Event );    // Event
					break;
				case Event:
					size = 2    // Flags
							+ stridx_size // Name
							+ getCodedIndexSize( CodedIndex.TypeDefOrRef );    // EventType
					break;
				case PropertyMap:
					size = getTableIndexSize( Table.TypeDef )    // Parent
							+ getTableIndexSize( Table.Property );    // PropertyList
					break;
				case PropertyPtr:
					size = getTableIndexSize( Table.Property );    // Property
					break;
				case Property:
					size = 2    // Flags
							+ stridx_size    // Name
							+ blobidx_size;    // Type
					break;
				case MethodSemantics:
					size = 2    // Semantics
							+ getTableIndexSize( Table.Method )    // Method
							+ getCodedIndexSize( CodedIndex.HasSemantics );    // Association
					break;
				case MethodImpl:
					size = getTableIndexSize( Table.TypeDef )    // Class
							+ getCodedIndexSize( CodedIndex.MethodDefOrRef )    // MethodBody
							+ getCodedIndexSize( CodedIndex.MethodDefOrRef );    // MethodDeclaration
					break;
				case ModuleRef:
					size = stridx_size;    // Name
					break;
				case TypeSpec:
					size = blobidx_size;    // Signature
					break;
				case ImplMap:
					size = 2    // MappingFlags
							+ getCodedIndexSize( CodedIndex.MemberForwarded )    // MemberForwarded
							+ stridx_size    // ImportName
							+ getTableIndexSize( Table.ModuleRef );    // ImportScope
					break;
				case FieldRVA:
					size = 4    // RVA
							+ getTableIndexSize( Table.Field );    // Field
					break;
				case EncLog:
					size = 8;
					break;
				case EncMap:
					size = 4;
					break;
				case Assembly:
					size = 16 // HashAlgId 4, Version 4 * 2, Flags 4
							+ blobidx_size    // PublicKey
							+ ( stridx_size * 2 );    // Name, Culture
					break;
				case AssemblyProcessor:
					size = 4;    // Processor
					break;
				case AssemblyOS:
					size = 12;    // Platform 4, Version 2 * 4
					break;
				case AssemblyRef:
					size = 12    // Version 2 * 4 + Flags 4
							+ ( blobidx_size * 2 )    // PublicKeyOrToken, HashValue
							+ ( stridx_size * 2 );    // Name, Culture
					break;
				case AssemblyRefProcessor:
					size = 4    // Processor
							+ getTableIndexSize( Table.AssemblyRef );    // AssemblyRef
					break;
				case AssemblyRefOS:
					size = 12    // Platform 4, Version 2 * 4
							+ getTableIndexSize( Table.AssemblyRef );    // AssemblyRef
					break;
				case File:
					size = 4    // Flags
							+ stridx_size    // Name
							+ blobidx_size;    // HashValue
					break;
				case ExportedType:
					size = 8    // Flags 4, TypeDefId 4
							+ ( stridx_size * 2 )    // Name, Namespace
							+ getCodedIndexSize( CodedIndex.Implementation );    // Implementation
					break;
				case ManifestResource:
					size = 8    // Offset, Flags
							+ stridx_size    // Name
							+ getCodedIndexSize( CodedIndex.Implementation );    // Implementation
					break;
				case NestedClass:
					size = getTableIndexSize( Table.TypeDef )    // NestedClass
							+ getTableIndexSize( Table.TypeDef );    // EnclosingClass
					break;
				case GenericParam:
					size = 4    // Number, Flags
							+ getCodedIndexSize( CodedIndex.TypeOrMethodDef )    // Owner
							+ stridx_size;    // Name
					break;
				case MethodSpec:
					size = getCodedIndexSize( CodedIndex.MethodDefOrRef )    // Method
							+ blobidx_size;    // Instantiation
					break;
				case GenericParamConstraint:
					size = getTableIndexSize( Table.GenericParam )    // Owner
							+ getCodedIndexSize( CodedIndex.TypeDefOrRef );    // Constraint
					break;
				default:
					throw new IllegalArgumentException();
			}

			TableHeap.Table heapTable = heap.getTable( table );
			heapTable.setOffset( offset );
			heapTable.setRowSize( size );

			offset += size * heapTable.getLength();
		}
	}

	private int getTableIndexSize( Table table )
	{
		//noinspection MagicNumber
		return image.getTableLength( table ) < 65536 ? 2 : 4;
	}

	private int getCodedIndexSize( CodedIndex index )
	{
		return image.getCodedIndexSize( index );
	}

	@SuppressWarnings( "unused" )
	public static Image readImageFromFile( File file ) throws IOException
	{
		byte[] bytes = Utils.readFileContent( file );
		return readImageFromBytes( file.getAbsolutePath(), bytes );
	}

	@SuppressWarnings( "unused" )
	public static Image readImageFromStream( String filename, InputStream is ) throws IOException
	{
		byte[] bytes = Utils.readStreamContent( is );
		return readImageFromBytes( filename, bytes );
	}

	public static Image readImageFromBytes( String filename, byte[] bytes )
	{
		ImageReader reader = new ImageReader( filename, bytes );
		reader.readImage();
		return reader.getImage();
	}

}
