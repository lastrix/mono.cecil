package mono.cecil.pe;

import mono.cecil.CodedIndex;
import mono.cecil.ModuleKind;
import mono.cecil.Table;
import mono.cecil.TargetArchitecture;
import mono.cecil.metadata.*;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings( {"ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter", "unused", "NestedAssignment"} )
public class Image
{
	private final int[] codedIndexSizes = new int[CodedIndex.values().length];


	// ******************************* Attributes ******************************************************************* //
	public String getHash()
	{
		return hash;
	}

	public void setHash( String hash )
	{
		this.hash = hash;
	}

	public ModuleKind getKind()
	{
		return kind;
	}

	public void setKind( ModuleKind kind )
	{
		this.kind = kind;
	}

	public String getRuntimeVersion()
	{
		return runtimeVersion;
	}

	public void setRuntimeVersion( String runtimeVersion )
	{
		this.runtimeVersion = runtimeVersion;
	}

	public TargetArchitecture getArchitecture()
	{
		return architecture;
	}

	public void setArchitecture( TargetArchitecture architecture )
	{
		this.architecture = architecture;
	}

	public int getCharacteristics()
	{
		return characteristics;
	}

	public void setCharacteristics( int characteristics )
	{
		this.characteristics = characteristics;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName( String fileName )
	{
		this.fileName = fileName;
	}

	public List<Section> getSections()
	{
		return sections;
	}

	public void setSections( List<Section> sections )
	{
		this.sections = sections;
	}

	public Section getMetadataSection()
	{
		return metadataSection;
	}

	public void setMetadataSection( Section metadataSection )
	{
		this.metadataSection = metadataSection;
	}

	public int getEntryPointToken()
	{
		return entryPointToken;
	}

	public void setEntryPointToken( int entryPointToken )
	{
		this.entryPointToken = entryPointToken;
	}

	public int getAttributes()
	{
		return attributes;
	}

	public void setAttributes( int attributes )
	{
		this.attributes = attributes;
	}

	public DataDirectory getDebug()
	{
		return debug;
	}

	public void setDebug( DataDirectory debug )
	{
		this.debug = debug;
	}

	public DataDirectory getResources()
	{
		return resources;
	}

	public void setResources( DataDirectory resources )
	{
		this.resources = resources;
	}

	public DataDirectory getStrongName()
	{
		return strongName;
	}

	public void setStrongName( DataDirectory strongName )
	{
		this.strongName = strongName;
	}

	public StringHeap getStringHeap()
	{
		return stringHeap;
	}

	public void setStringHeap( StringHeap stringHeap )
	{
		this.stringHeap = stringHeap;
	}

	public BlobHeap getBlobHeap()
	{
		return blobHeap;
	}

	public void setBlobHeap( BlobHeap blobHeap )
	{
		this.blobHeap = blobHeap;
	}

	public UserStringHeap getUserStringHeap()
	{
		return userStringHeap;
	}

	public void setUserStringHeap( UserStringHeap userStringHeap )
	{
		this.userStringHeap = userStringHeap;
	}

	public GuidHeap getGuidHeap()
	{
		return guidHeap;
	}

	public void setGuidHeap( GuidHeap guidHeap )
	{
		this.guidHeap = guidHeap;
	}

	public TableHeap getTableHeap()
	{
		return tableHeap;
	}

	public void setTableHeap( TableHeap tableHeap )
	{
		this.tableHeap = tableHeap;
	}

	private String hash;

	private ModuleKind kind;
	private String runtimeVersion;
	private TargetArchitecture architecture;
	private int characteristics; // ModuleCharacteristics
	private String fileName;

	private List<Section> sections = new ArrayList<>();

	private Section metadataSection;

	private int entryPointToken;
	private int attributes;

	private DataDirectory debug;
	private DataDirectory resources;
	private DataDirectory strongName;

	private StringHeap stringHeap;
	private BlobHeap blobHeap;
	private UserStringHeap userStringHeap;
	private GuidHeap guidHeap;
	private TableHeap tableHeap;

	// ******************************* Operations ******************************************************************* //
	public boolean hasTable( Table table )
	{
		return getTableLength( table ) > 0;
	}

	public int getTableLength( Table table )
	{
		return tableHeap.getTable( table ).getLength();
	}

	public int getTableIndexSize( Table table )
	{
		//noinspection MagicNumber
		return getTableLength( table ) < 65536 ? 2 : 4;
	}

	public int getCodedIndexSize( CodedIndex codedIndex )
	{
		int index = codedIndex.ordinal();
		int size = codedIndexSizes[index];
		if( size != 0 )
			return size;

		return codedIndexSizes[index] = getCodedIndexSizeImpl( codedIndex );
	}

	public int resolveVirtualAddress( int rva )
	{
		Section section = getSectionAtVirtualAddress( rva );
		if( section == null )
			throw new IllegalArgumentException();

		return resolveVirtualAddressInSection( rva, section );
	}

	private static int resolveVirtualAddressInSection( int rva, Section section )
	{
		return rva + section.pointerToRawData() - section.virtualAddress();
	}

	public Section getSection( String name )
	{
		for( Section section : sections )
		{
			if( section.name().equals( name ) )
				return section;
		}
		return null;
	}

	public Section getSectionAtVirtualAddress( int rva )
	{
		for( Section section : sections )
		{
			if( rva >= section.virtualAddress() && rva < section.virtualAddress() + section.sizeOfRawData() )
				return section;

		}
		return null;
	}

//	public ImageDebugDirectory getDebugHeader (byte [] header)
//	{
//		Section section = getSectionAtVirtualAddress (debug.getVirtualAddress());
//		ByteBuffer buffer = section.data();
//		buffer.offset( debug.getVirtualAddress() - section.virtualAddress() );
//
//		var directory = new ImageDebugDirectory {
//		Characteristics = buffer.ReadInt32 (),
//				TimeDateStamp = buffer.ReadInt32 (),
//				MajorVersion = buffer.ReadInt16 (),
//				MinorVersion = buffer.ReadInt16 (),
//				Type = buffer.ReadInt32 (),
//				SizeOfData = buffer.ReadInt32 (),
//				AddressOfRawData = buffer.ReadInt32 (),
//				PointerToRawData = buffer.ReadInt32 (),
//	};
//
//		if (directory.SizeOfData == 0 || directory.PointerToRawData == 0) {
//			header = Empty<byte>.Array;
//			return directory;
//		}
//
//		buffer.position = (int) (directory.PointerToRawData - section.PointerToRawData);
//
//		header = new byte [directory.SizeOfData];
//		Buffer.BlockCopy (buffer.buffer, buffer.position, header, 0, header.Length);
//
//		return directory;
//	}

	// ******************************* Privates ********************************************************************* //
	private int getCodedIndexSizeImpl( CodedIndex codedIndex )
	{
		int bits;
		Table[] tables;

		switch( codedIndex )
		{
			case TypeDefOrRef:
				bits = 2;
				tables = new Table[]{Table.TypeDef, Table.TypeRef, Table.TypeSpec};
				break;
			case HasConstant:
				bits = 2;
				tables = new Table[]{Table.Field, Table.Param, Table.Property};
				break;
			case HasCustomAttribute:
				bits = 5;
				tables = new Table[]{
						Table.Method, Table.Field, Table.TypeRef, Table.TypeDef, Table.Param, Table.InterfaceImpl, Table.MemberRef,
						Table.Module, Table.DeclSecurity, Table.Property, Table.Event, Table.StandAloneSig, Table.ModuleRef,
						Table.TypeSpec, Table.Assembly, Table.AssemblyRef, Table.File, Table.ExportedType,
						Table.ManifestResource, Table.GenericParam
				};
				break;
			case HasFieldMarshal:
				bits = 1;
				tables = new Table[]{Table.Field, Table.Param};
				break;
			case HasDeclSecurity:
				bits = 2;
				tables = new Table[]{Table.TypeDef, Table.Method, Table.Assembly};
				break;
			case MemberRefParent:
				bits = 3;
				tables = new Table[]{Table.TypeDef, Table.TypeRef, Table.ModuleRef, Table.Method, Table.TypeSpec};
				break;
			case HasSemantics:
				bits = 1;
				tables = new Table[]{Table.Event, Table.Property};
				break;
			case MethodDefOrRef:
				bits = 1;
				tables = new Table[]{Table.Method, Table.MemberRef};
				break;
			case MemberForwarded:
				bits = 1;
				tables = new Table[]{Table.Field, Table.Method};
				break;
			case Implementation:
				bits = 2;
				tables = new Table[]{Table.File, Table.AssemblyRef, Table.ExportedType};
				break;
			case CustomAttributeType:
				bits = 3;
				tables = new Table[]{Table.Method, Table.MemberRef};
				break;
			case ResolutionScope:
				bits = 2;
				tables = new Table[]{Table.Module, Table.ModuleRef, Table.AssemblyRef, Table.TypeRef};
				break;
			case TypeOrMethodDef:
				bits = 1;
				tables = new Table[]{Table.TypeDef, Table.Method};
				break;
			default:
				throw new IllegalArgumentException();
		}

		int max = 0;

		for( Table table : tables )
			max = Math.max( max, getTableLength( table ) );

		//noinspection MagicNumber
		return max < ( 1 << ( 16 - bits ) ) ? 2 : 4;
	}
}
