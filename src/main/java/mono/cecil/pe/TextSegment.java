package mono.cecil.pe;

public enum TextSegment
{
	ImportAddressTable,
	CLIHeader,
	Code,
	Resources,
	Data,
	StrongNameSignature,

	// Metadata
	MetadataHeader,
	TableHeap,
	StringHeap,
	UserStringHeap,
	GuidHeap,
	BlobHeap,
	// End Metadata

	DebugDirectory,
	ImportDirectory,
	ImportHintNameTable,
	StartupStub
}
