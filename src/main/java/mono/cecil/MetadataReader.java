package mono.cecil;


import mono.cecil.metadata.BlobHeap;
import mono.cecil.metadata.MetadataToken;
import mono.cecil.metadata.TableHeap;
import mono.cecil.metadata.rows.Row2;
import mono.cecil.metadata.rows.Row3;
import mono.cecil.pe.ByteBuffer;
import mono.cecil.pe.Image;
import mono.cecil.pe.Section;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

@SuppressWarnings({"WeakerAccess", "unused", "TypeMayBeWeakened", "MagicNumber"})
public final class MetadataReader extends ByteBuffer implements IDisposable {
    public MetadataReader(ModuleDefinition module) {
        super(module.getImage().getMetadataSection().data().bytes());
        this.module = module;
        image = module.getImage();
        metadata = module.getMetadata();
    }

    private Image image;
    private ModuleDefinition module;
    private MetadataSystem metadata;

    private IGenericContext context;
    //internal CodeReader code;


    @Override
    public void dispose() {
        image = null;
        module = null;
        if (metadata != null) {
            metadata.dispose();
            metadata = null;
        }
    }

    public ModuleDefinition getModule() {
        return module;
    }

    public Image getImage() {
        return image;
    }

    public IGenericContext getContext() {
        return context;
    }

    public void setContext(IGenericContext context) {
        this.context = context;
    }


    public MetadataSystem getMetadata() {
        return metadata;
    }

    public int getCodedIndexSize(CodedIndex index) {
        return image.getCodedIndexSize(index);
    }

    public int readByIndexSize(int size) {
        if (size == 4)
            return readUInt32();
        else
            return readUInt16();
    }

    public byte[] readBlob() {
        BlobHeap blob_heap = image.getBlobHeap();
        if (blob_heap == null) {
            advance(2);
            return Utils.EMPTY_BYTE_ARRAY;
        }

        return blob_heap.read(readBlobIndex());
    }

    public byte[] readBlob(int signature) {
        BlobHeap blob_heap = image.getBlobHeap();
        if (blob_heap == null)
            return Utils.EMPTY_BYTE_ARRAY;

        return blob_heap.read(signature);
    }

    public int readBlobIndex() {
        BlobHeap blob_heap = image.getBlobHeap();
        return readByIndexSize(blob_heap != null ? blob_heap.getIndexSize() : 2);
    }

    public String readString() {
        return image.getStringHeap().read(readByIndexSize(image.getStringHeap().getIndexSize()));
    }

    public int readStringIndex() {
        return readByIndexSize(image.getStringHeap().getIndexSize());
    }

    public int readTableIndex(Table table) {
        return readByIndexSize(image.getTableIndexSize(table));
    }

    public MetadataToken readMetadataToken(CodedIndex index) {
        return index.getMetadataToken(readByIndexSize(getCodedIndexSize(index)));
    }

    public int moveTo(Table table) {
        TableHeap.Table info = image.getTableHeap().getTable(table);
        if (info.getLength() != 0)
            offset(info.getOffset());

        return info.getLength();
    }

    public boolean moveTo(Table table, int row) {
        TableHeap.Table info = image.getTableHeap().getTable(table);
        int length = info.getLength();
        if (length == 0 || row > length)
            return false;

        offset(info.getOffset() + (info.getRowSize() * (row - 1)));
        return true;
    }

    @Nullable
    public AssemblyNameDefinition readAssemblyNameDefinition() {
        if (moveTo(Table.Assembly) == 0)
            return null;

        AssemblyNameDefinition name = new AssemblyNameDefinition();

        name.setHashAlgorithm(AssemblyHashAlgorithm.getByCode(readUInt32()));

        populateVersionAndFlags(name);

        name.setPublicKey(readBlob());

        populateNameAndCulture(name);

        return name;
    }

    public ModuleDefinition populate(ModuleDefinition module) {
        if (moveTo(Table.Module) == 0)
            return module;

        advance(2); // Generation

        module.setName(readString());
        module.setMvid(image.getGuidHeap().read(readByIndexSize(image.getGuidHeap().getIndexSize())));

        return module;
    }

    void initializeAssemblyReferences() {
        if (metadata.hasAssemblyReferences())
            return;

        int length = moveTo(Table.AssemblyRef);
        metadata.setAssemblyReferences(length);

        for (int i = 0; i < length; i++) {
            AssemblyNameReference reference = new AssemblyNameReference();
            reference.setMetadataToken(new MetadataToken(TokenType.AssemblyRef, i + 1));

            populateVersionAndFlags(reference);

            byte[] key_or_token = readBlob();

            if (AssemblyAttributes.PublicKey.isSet(reference.getAttributes()))
                reference.setPublicKey(key_or_token);
            else
                reference.setPublicKeyToken(key_or_token);

            populateNameAndCulture(reference);

            reference.setHash(readBlob());

            metadata.setAssemblyReference(i, reference);
        }
    }

    public Collection<AssemblyNameReference> readAssemblyReferences() {
        initializeAssemblyReferences();

        return metadata.getAssemblyReferences();
    }

    @Nullable
    public MethodDefinition readEntryPoint() {
        if (module.getImage().getEntryPointToken() == 0)
            return null;

        MetadataToken token = new MetadataToken(module.getImage().getEntryPointToken());
        return getMethodDefinition(token.getRid());
    }

    public Collection<ModuleDefinition> readModules() {
        Collection<ModuleDefinition> modules = new ArrayList<>(1);
        modules.add(module);

        int length = moveTo(Table.File);
        for (int i = 1; i <= length; i++) {
            int attributes = readUInt32();
            String name = readString();
            readBlobIndex();

            if (FileAttributes.ContainsMetaData.ordinal() != attributes)
                continue;

            String moduleFileName = getModuleFileName(name, new File(module.getImage().getFileName()).getParentFile());
            // hack the system via caching and deferred reading
            if (module.getAssemblyResolver() instanceof INetModuleResolver) {
                ModuleDefinition netModule = ((INetModuleResolver) module.getAssemblyResolver()).resolveNetModule(moduleFileName);
                if (netModule == null) {
                    try {
                        netModule = ModuleDefinition.readModule(moduleFileName);
                        if (netModule != null)
                            ((INetModuleResolver) module.getAssemblyResolver()).registerNetModule(moduleFileName, netModule);
                    } catch (Exception ignored) {

                    }
                }
                if (netModule != null) {
                    modules.add(netModule);
                    continue;
                }
            }

            if (new File(moduleFileName).exists()) {
                ReaderParameters parameters = new ReaderParameters();
                parameters.setReadingMode(module.getReadingMode());
                parameters.setAssemblyResolver(module.getAssemblyResolver());
                modules.add(ModuleDefinition.readModule(moduleFileName, parameters));
            }
        }

        return modules;
    }

    public String getModuleFileName(String name, File parentFile) {
        if (module.getFullyQualifiedName() == null)
            throw new UnsupportedOperationException();

        //noinspection HardcodedFileSeparator
        return new File(parentFile, name.replace('\\', File.separatorChar)).getAbsolutePath();
    }

    void initializeModuleReferences() {
        if (metadata.hasModuleReferences())
            return;

        int length = moveTo(Table.ModuleRef);
        metadata.setModuleReferences(length);

        for (int i = 0; i < length; i++) {
            ModuleReference reference = new ModuleReference(readString());
            reference.setMetadataToken(new MetadataToken(TokenType.ModuleRef, i + 1));

            metadata.setModuleReference(i, reference);
        }
    }

    public List<ModuleReference> readModuleReferences() {
        initializeModuleReferences();

        return metadata.getModuleReferences();
    }

    public boolean hasFileResource() {
        int length = moveTo(Table.File);
        if (length == 0)
            return false;

        for (int i = 1; i <= length; i++)
            if (readFileRecord(i).getCol1() == FileAttributes.ContainsNoMetaData)
                return true;

        return false;
    }

    public Collection<Resource> readResources() {
        int length = moveTo(Table.ManifestResource);
        Collection<Resource> resources = new ArrayList<>(length);

        for (int i = 1; i <= length; i++) {
            int offset = readUInt32();
            int flags = readUInt32();
            String name = readString();
            MetadataToken implementation = readMetadataToken(CodedIndex.Implementation);

            Resource resource;

            if (implementation.getRid() == 0) {
                resource = new EmbeddedResource(name, flags, offset, this);
            } else if (implementation.getTokenType() == TokenType.AssemblyRef) {
                resource = new AssemblyLinkedResource(name, flags);
                ((AssemblyLinkedResource) resource).setAssembly((AssemblyNameReference) getTypeReferenceScope(implementation));
            } else if (implementation.getTokenType() == TokenType.File) {
                Row3<FileAttributes, String, Integer> file_record = readFileRecord(implementation.getRid());

                resource = new LinkedResource(name, flags);
                ((LinkedResource) resource).setFile(file_record.getCol2());
                ((LinkedResource) resource).setHash(readBlob(file_record.getCol3()));
            } else
                throw new UnsupportedOperationException();

            resources.add(resource);
        }

        return resources;
    }

    Row3<FileAttributes, String, Integer> readFileRecord(int rid) {
        int position = position();

        if (!moveTo(Table.File, rid))
            throw new IllegalArgumentException();

        Row3<FileAttributes, String, Integer> record = new Row3<>(
                FileAttributes.values()[readUInt32()],
                readString(),
                readBlobIndex());

        offset(position);

        return record;
    }

    public ByteBuffer getManagedResourceStream(int offset) {
        int rva = image.getResources().getVirtualAddress();
        Section section = image.getSectionAtVirtualAddress(rva);
        int position = (rva - section.virtualAddress()) + offset;
        byte[] buffer = section.data().bytes();


        //noinspection MagicNumber
        int length = buffer[position]
                | (buffer[position + 1] << 8)
                | (buffer[position + 2] << 16)
                | (buffer[position + 3] << 24);

        // FIXME: Нужно сделать возможность работать без копирования
        byte[] data = new byte[length];
        System.arraycopy(buffer, position + 4, data, 0, length);
        return new ByteBuffer(data);
    }

    void populateVersionAndFlags(AssemblyNameReference name) {
        name.setVersion(new Version(
                readUInt16(),
                readUInt16(),
                readUInt16(),
                readUInt16()));

        name.setAttributes(readUInt32());
    }

    void populateNameAndCulture(AssemblyNameReference name) {
        name.setName(readString());
        name.setCulture(readString());
    }

    public TypeDefinitionCollection readTypes() {
        initializeTypeDefinitions();
        TypeDefinition[] mtypes = metadata.getTypes();
        int type_count = mtypes.length - metadata.getNestedTypesSize();
        TypeDefinitionCollection types = new TypeDefinitionCollection(type_count, module);

        for (TypeDefinition mtype : mtypes) {
            if (mtype == null)
                continue; // not initialized

            if (isNested(mtype.getAttributes()))
                continue;

            types.add(mtype);
        }


        if (image.hasTable(Table.MethodPtr) || image.hasTable(Table.FieldPtr))
            completeTypes();

        return types;
    }

    void completeTypes() {
        TypeDefinition[] types = metadata.getTypes();

        for (TypeDefinition type : types) {
            initializeCollection(type.getFields());
            initializeCollection(type.getMethods());
        }
    }

    void initializeTypeDefinitions() {
        if (metadata.hasTypeDefinitions())
            return;

        initializeNestedTypes();
        initializeFields();
        initializeMethods();

        int length = moveTo(Table.TypeDef);
        metadata.setTypeDefinitions(length);

        for (int i = 0; i < length; i++)
            readType(i + 1);
    }

    private static boolean isNested(int attributes) {

        switch (TypeAttributes.getByCode(attributes & TypeAttributes.VisibilityMask.getValue())) {
            case NestedAssembly:
            case NestedFamANDAssem:
            case NestedFamily:
            case NestedFamORAssem:
            case NestedPrivate:
            case NestedPublic:
                return true;
            default:
                return false;
        }
    }

    public boolean hasNestedTypes(TypeDefinition type) {
        initializeNestedTypes();
        return ArrayUtils.isNotEmpty(metadata.getNestedType(type.getMetadataToken().getRid()));
    }

    public Collection<TypeDefinition> readNestedTypes(TypeDefinition type) {
        initializeNestedTypes();
        Integer[] items = metadata.getNestedType(type.getMetadataToken().getRid());
        if (items == null)
            return new MemberDefinitionCollection<>(type);

        MemberDefinitionCollection<TypeDefinition> nestedTypes = new MemberDefinitionCollection<>(items.length, type);

        for (Integer item : items) {
            TypeDefinition nestedType = getTypeDefinition(item);

            if (nestedType != null)
                nestedTypes.add(nestedType);
        }

        metadata.removeNestedType(type.getMetadataToken().getRid());
        return nestedTypes;
    }

    void initializeNestedTypes() {
        if (metadata.hasNestedTypes())
            return;

        int length = moveTo(Table.NestedClass);

        metadata.setNestedTypes(length);
        metadata.setReverseNestedTypes(length);

        if (length == 0)
            return;

        for (int i = 1; i <= length; i++) {
            int nested = readTableIndex(Table.TypeDef);
            int declaring = readTableIndex(Table.TypeDef);

            metadata.addNestedType(declaring, nested);
            metadata.setReverseNestedType(nested, declaring);
        }
    }

    @Nullable
    private TypeDefinition readType(int rid) {
        if (!moveTo(Table.TypeDef, rid))
            return null;

        if (metadata.getTypeDefinition(rid) != null)
            return metadata.getTypeDefinition(rid);

        int attributes = readUInt32();
        String name = readString();
        String namespace = readString();
        TypeDefinition type = new TypeDefinition(namespace, name, attributes);
        type.setMetadataToken(new MetadataToken(TokenType.TypeDef, rid));
        type.setScope(module);
        type.setModule(module);

        metadata.addTypeDefinition(type);

        context = type;
        type.setBaseType(getTypeDefOrRef(readMetadataToken(CodedIndex.TypeDefOrRef)));

        type.setFieldsRange(readFieldsRange(rid));
        type.setMethodsRange(readMethodsRange(rid));

        if (isNested(attributes))
            //noinspection ConstantConditions
            type.setDeclaringType(getNestedTypeDeclaringType(type));

        return type;
    }

    @Nullable
    private TypeDefinition getNestedTypeDeclaringType(TypeDefinition type) {
        Integer declaring_rid = metadata.getReverseNestedType(type);
        if (declaring_rid == null)
            return null;

        metadata.removeReverseNestedType(type);
        return getTypeDefinition(declaring_rid);
    }

    private Range readFieldsRange(int type_index) {
        return readListRange(type_index, Table.TypeDef, Table.Field);
    }

    private Range readMethodsRange(int type_index) {
        return readListRange(type_index, Table.TypeDef, Table.Method);
    }

    private Range readListRange(int current_index, Table current, Table target) {
        Range list = new Range();

        list.setStart(readTableIndex(target));

        int next_index;
        TableHeap.Table current_table = image.getTableHeap().getTable(current);

        if (current_index == current_table.getLength())
            next_index = image.getTableHeap().getTable(target).getLength() + 1;
        else {
            int position = position();
            advance(current_table.getRowSize() - image.getTableIndexSize(target));
            next_index = readTableIndex(target);
            offset(position);
        }

        list.setLength(next_index - list.getStart());
        return list;
    }

    public Row2<Integer, Integer> readTypeLayout(TypeDefinition type) {
        initializeTypeLayouts();
        int rid = type.getMetadataToken().getRid();
        Row2<Integer, Integer> class_layout = metadata.getClassLayoutRow(rid);
        if (class_layout == null)
            return new Row2<>(Utils.NO_DATA_MARK, Utils.NO_DATA_MARK);

        type.setPackingSize(class_layout.getCol1());
        type.setClassSize(class_layout.getCol2());

        metadata.removeClassLayoutRow(rid);

        return new Row2<>(class_layout.getCol1(), class_layout.getCol2());
    }

    private void initializeTypeLayouts() {
        if (metadata.hasClassLayoutRows())
            return;

        int length = moveTo(Table.ClassLayout);

        metadata.setClassLayoutRows(length);

        for (int i = 0; i < length; i++) {
            int packing_size = readUInt16();
            int class_size = readUInt32();

            int parent = readTableIndex(Table.TypeDef);

            metadata.setClassLayoutRow(parent, new Row2<>(packing_size, class_size));
        }
    }

    @Nullable
    public TypeReference getTypeDefOrRef(MetadataToken token) {
        return (TypeReference) lookupToken(token);
    }

    @Nullable
    public TypeDefinition getTypeDefinition(int rid) {
        initializeTypeDefinitions();

        TypeDefinition type = metadata.getTypeDefinition(rid);
        if (type != null)
            return type;

        return readTypeDefinition(rid);
    }

    @Nullable
    TypeDefinition readTypeDefinition(int rid) {
        if (!moveTo(Table.TypeDef, rid))
            return null;

        return readType(rid);
    }

    void initializeTypeReferences() {
        if (metadata.hasTypeReferences())
            return;

        metadata.setTypeReferences(image.getTableLength(Table.TypeRef));
    }

    @Nullable
    public TypeReference getTypeReference(@Nullable String scope, String full_name) {
        initializeTypeReferences();

        int length = metadata.getTypeReferencesLength();

        for (int i = 1; i <= length; i++) {
            TypeReference type = getTypeReference(i);

            if (!type.getFullName().equals(full_name))
                continue;

            if (StringUtils.isBlank(scope))
                return type;

            if (type.getScope().getName().equals(scope))
                return type;
        }

        return null;
    }

    @Nullable
    private TypeReference getTypeReference(int rid) {
        initializeTypeReferences();

        TypeReference type = metadata.getTypeReference(rid);
        if (type != null)
            return type;

        return readTypeReference(rid);
    }

    @Nullable
    private TypeReference readTypeReference(int rid) {
        if (!moveTo(Table.TypeRef, rid))
            return null;

        MetadataToken scope_token = readMetadataToken(CodedIndex.ResolutionScope);

        String name = readString();
        String namespace = readString();

        TypeReference type = new TypeReference(namespace, name, module, null);

        type.setMetadataToken(new MetadataToken(TokenType.TypeRef, rid));

        metadata.addTypeReference(type);

        TypeReference declaring_type = null;
        IMetadataScope scope;
        if (scope_token.getTokenType() == TokenType.TypeRef) {
            declaring_type = getTypeDefOrRef(scope_token);

            scope = declaring_type != null
                    ? declaring_type.getScope()
                    : module;
        } else
            scope = getTypeReferenceScope(scope_token);

        //noinspection ConstantConditions
        type.setScope(scope);
        //noinspection ConstantConditions
        type.setDeclaringType(declaring_type);

        MetadataSystem.tryProcessPrimitiveTypeReference(type);

        return type;
    }

    @Nullable
    private IMetadataScope getTypeReferenceScope(MetadataToken scope) {
        if (scope.getTokenType() == TokenType.Module)
            return module;

        List<? extends IMetadataScope> scopes;

        switch (scope.getTokenType()) {
            case AssemblyRef:
                initializeAssemblyReferences();
                scopes = metadata.getAssemblyReferences();
                break;
            case ModuleRef:
                initializeModuleReferences();
                scopes = metadata.getModuleReferences();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        int index = scope.getRid() - 1;
        if (index < 0 || index >= scopes.size())
            return null;

        return scopes.get(index);
    }

    public Collection<TypeReference> getTypeReferences() {
        initializeTypeReferences();

        int length = image.getTableLength(Table.TypeRef);

        Collection<TypeReference> type_references = new ArrayList<>(length);

        for (int i = 1; i <= length; i++)
            type_references.add(getTypeReference(i));

        return type_references;
    }

    private final Deque<Integer> typeSpecificationStack = new LinkedList<>();

    private TypeReference getTypeSpecification(int rid) {
        if (!moveTo(Table.TypeSpec, rid))
            return null;

        if (typeSpecificationStack.contains(rid))
            throw new IllegalArgumentException("Circular reference");

        typeSpecificationStack.push(rid);
        SignatureReader reader = readSignature(readBlobIndex());
        TypeReference type = reader.readTypeSignature();
        if (type.getMetadataToken().getRid() == 0)
            type.setMetadataToken(new MetadataToken(TokenType.TypeSpec, rid));

        if (typeSpecificationStack.pop() != rid)
            throw new IllegalStateException("Invalid rid returned.");

        return type;
    }

    private SignatureReader readSignature(int signature) {
        return new SignatureReader(signature, this);
    }

    public boolean hasInterfaces(TypeDefinition type) {
        initializeInterfaces();
        return metadata.getInterfaces(type) != null;
    }

    public Collection<TypeReference> readInterfaces(TypeDefinition type) {
        initializeInterfaces();
        MetadataToken[] tokens = metadata.getInterfaces(type);

        if (tokens == null)
            return Collections.emptyList();

        Collection<TypeReference> interfaces = new ArrayList<>(tokens.length);

        context = type;

        for (MetadataToken token : tokens)
            interfaces.add(getTypeDefOrRef(token));

        metadata.removeInterfaces(type.getMetadataToken().getRid());

        return interfaces;
    }

    private void initializeInterfaces() {
        if (metadata.hasInterfaces())
            return;

        int length = moveTo(Table.InterfaceImpl);

        metadata.setInterfaces(length);

        for (int i = 0; i < length; i++) {
            int type = readTableIndex(Table.TypeDef);
            MetadataToken token = readMetadataToken(CodedIndex.TypeDefOrRef);

            metadata.addInterfaces(type, token);
        }
    }

    public Collection<FieldDefinition> readFields(TypeDefinition type) {
        Range fields_range = type.getFieldsRange();
        if (fields_range.getLength() == 0)
            return new MemberDefinitionCollection<>(type);

        MemberDefinitionCollection<FieldDefinition> fields = new MemberDefinitionCollection<>(fields_range.getLength(), type);
        context = type;

        if (moveTo(Table.FieldPtr, fields_range.getStart()))
            readPointers(Table.FieldPtr, Table.Field, fields_range, fields, this::readField);
        else {
            if (!moveTo(Table.Field, fields_range.getStart()))
                return fields;

            for (int i = 0; i < fields_range.getLength(); i++)
                readField(fields_range.getStart() + i, fields);
        }

        return fields;
    }

    void readField(int field_rid, Collection<FieldDefinition> fields) {
        int attributes = readUInt16();
        String name = readString();
        int signature = readBlobIndex();

        FieldDefinition field = new FieldDefinition(name, attributes, readFieldType(signature));
        field.setMetadataToken(new MetadataToken(TokenType.Field, field_rid));
        metadata.addFieldDefinition(field);

        if (isDeleted(field))
            return;

        fields.add(field);
    }

    private void initializeFields() {
        if (metadata.hasFieldDefinitions())
            return;

        metadata.setFieldDefinitions(image.getTableLength(Table.Field));
    }

    private TypeReference readFieldType(int signature) {
        SignatureReader reader = readSignature(signature);

        int field_sig = 0x6;

        if (reader.readByte() != field_sig)
            throw new UnsupportedOperationException();

        return reader.readTypeSignature();
    }

    public int readFieldRVA(FieldDefinition field) {
        initializeFieldRVAs();
        int rid = field.getMetadataToken().getRid();

        Integer rva = metadata.getFieldRva(rid);
        if (rva == null)
            return 0;

        int size = getFieldTypeSize(field.getFieldType());

        if (size == 0 || rva == 0)
            return 0;

        metadata.removeFieldRva(rid);

        field.setInitialValue(getFieldInitializeValue(size, rva));
        return rva;
    }

    private byte[] getFieldInitializeValue(int size, int rva) {
        Section section = image.getSectionAtVirtualAddress(rva);
        if (section == null)
            return Utils.EMPTY_BYTE_ARRAY;

        byte[] value = new byte[size];
        System.arraycopy(section.data().bytes(), rva - section.virtualAddress(), value, 0, size);
        return value;
    }

    private static int getFieldTypeSize(TypeReference type) {
        int size = 0;

        switch (type.getEtype()) {
            case Boolean:
            case U1:
            case I1:
                size = 1;
                break;
            case U2:
            case I2:
            case Char:
                size = 2;
                break;
            case U4:
            case I4:
            case R4:
                size = 4;
                break;
            case U8:
            case I8:
            case R8:
                size = 8;
                break;
            case Ptr:
            case FnPtr:
                // TODO: нужно смотреть на целевую платформу модуля!!!!!
                size = 4;//8;
                break;
            case CModOpt:
            case CModReqD:
                return getFieldTypeSize(((IModifierType) type).getElementType());
            default:
                TypeDefinition field_type = type.resolve();
                if (field_type != null && field_type.hasLayoutInfo())
                    size = field_type.getClassSize();
                break;
        }

        return size;
    }

    private void initializeFieldRVAs() {
        if (metadata.hasFieldRvas())
            return;

        int length = moveTo(Table.FieldRVA);

        metadata.setFieldRvas(length);

        for (int i = 0; i < length; i++) {
            int rva = readUInt32();
            int field = readTableIndex(Table.Field);

            metadata.setFieldRva(field, rva);
        }
    }

    public int readFieldLayout(FieldDefinition field) {
        initializeFieldLayouts();
        int rid = field.getMetadataToken().getRid();
        Integer offset = metadata.getFieldLayout(rid);
        if (offset == null)
            return Utils.NO_DATA_MARK;

        metadata.removeFieldLayout(rid);

        return offset;
    }

    private void initializeFieldLayouts() {
        if (metadata.hasFieldLayouts())
            return;

        int length = moveTo(Table.FieldLayout);

        metadata.setFieldLayouts(length);

        for (int i = 0; i < length; i++) {
            int offset = readUInt32();
            int field = readTableIndex(Table.Field);

            metadata.setFieldLayout(field, offset);
        }
    }

    public boolean hasEvents(TypeDefinition type) {
        initializeEvents();

        Range range = metadata.getEventRange(type);
        return range != null && range.getLength() > 0;

    }

    public Collection<EventDefinition> readEvents(TypeDefinition type) {
        initializeEvents();
        Range range = metadata.getEventRange(type);

        if (range == null)
            return new MemberDefinitionCollection<>(type);

        Collection<EventDefinition> events = new MemberDefinitionCollection<>(range.getLength(), type);

        metadata.removeEventRange(type);

        if (range.getLength() == 0)
            return events;

        context = type;

        if (moveTo(Table.EventPtr, range.getStart()))
            readPointers(Table.EventPtr, Table.Event, range, events, this::readEvent);
        else {
            if (!moveTo(Table.Event, range.getStart()))
                return events;

            for (int i = 0; i < range.getLength(); i++)
                readEvent(range.getStart() + i, events);
        }

        return events;
    }

    private void readEvent(int event_rid, Collection<EventDefinition> events) {
        int attributes = readUInt16();
        String name = readString();
        TypeReference event_type = getTypeDefOrRef(readMetadataToken(CodedIndex.TypeDefOrRef));

        EventDefinition event = new EventDefinition(name, event_type, attributes);
        event.setMetadataToken(new MetadataToken(TokenType.Event, event_rid));

        if (isDeleted(event))
            return;

        events.add(event);
    }

    void initializeEvents() {
        if (metadata.hasEventRanges())
            return;

        int length = moveTo(Table.EventMap);

        metadata.setEventRanges(length);

        for (int i = 1; i <= length; i++) {
            int type_rid = readTableIndex(Table.TypeDef);
            Range events_range = readEventsRange(i);
            metadata.setEventRange(type_rid, events_range);
        }
    }

    private Range readEventsRange(int rid) {
        return readListRange(rid, Table.EventMap, Table.Event);
    }

    public boolean hasProperties(TypeDefinition type) {
        initializeProperties();

        Range range = metadata.getPropertiesRange(type);
        return range != null && range.getLength() > 0;
    }

    public Collection<PropertyDefinition> readProperties(TypeDefinition type) {
        initializeProperties();

        Range range = metadata.getPropertiesRange(type);

        if (range == null)
            return new MemberDefinitionCollection<>(type);

        metadata.removePropertiesRange(type);

        Collection<PropertyDefinition> properties = new MemberDefinitionCollection<>(range.getLength(), type);

        if (range.getLength() == 0)
            return properties;

        context = type;

        if (moveTo(Table.PropertyPtr, range.getStart()))
            readPointers(Table.PropertyPtr, Table.Property, range, properties, this::readProperty);
        else {
            if (!moveTo(Table.Property, range.getStart()))
                return properties;
            for (int i = 0; i < range.getLength(); i++)
                readProperty(range.getStart() + i, properties);
        }

        return properties;
    }

    private void readProperty(int property_rid, Collection<PropertyDefinition> properties) {
        int attributes = readUInt16();
        String name = readString();
        int signature = readBlobIndex();

        SignatureReader reader = readSignature(signature);
        int property_signature = 0x8;

        int calling_convention = reader.readByte();

        if ((calling_convention & property_signature) == 0)
            throw new UnsupportedOperationException();

        boolean hasThis = (calling_convention & 0x20) != 0;

        reader.readCompressedUInt32(); // count

        PropertyDefinition property = new PropertyDefinition(name, attributes, reader.readTypeSignature());
        property.setHasThis(hasThis);
        property.setMetadataToken(new MetadataToken(TokenType.Property, property_rid));

        if (isDeleted(property))
            return;

        properties.add(property);
    }

    void initializeProperties() {
        if (metadata.hasProperties())
            return;

        int length = moveTo(Table.PropertyMap);

        metadata.setProperties(length);

        for (int i = 1; i <= length; i++) {
            int rid = readTableIndex(Table.TypeDef);
            Range range = readListRange(i, Table.PropertyMap, Table.Property);
            metadata.addPropertiesRange(rid, range);
        }
    }

    private int ReadMethodSemantics(MethodDefinition method) {
        initializeMethodSemantics();
        Row2<Integer, MetadataToken> row = metadata.getSemanticsRow(method.getMetadataToken().getRid());
        if (row == null)
            return MethodSemanticsAttributes.None.getMask();

        TypeDefinition type = method.getDeclaringType();

        switch (MethodSemanticsAttributes.getByCode(row.getCol1())) {
            case AddOn:
                getEvent(type, row.getCol2()).setAddMethod(method);
                break;
            case Fire:
                getEvent(type, row.getCol2()).setInvokeMethod(method);
                break;
            case RemoveOn:
                getEvent(type, row.getCol2()).setRemoveMethod(method);
                break;
            case Getter:
                getProperty(type, row.getCol2()).setGetMethod(method);
                break;

            case Setter:
                getProperty(type, row.getCol2()).setSetMethod(method);
                break;

            case Other:
                readOtherMethodSemantics(method, row, type);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        metadata.removeSemanticsRow(method.getMetadataToken().getRid());

        return row.getCol1();
    }

    private static void readOtherMethodSemantics(MethodDefinition method, Row2<Integer, MetadataToken> row, TypeDefinition type) {
        switch (row.getCol2().getTokenType()) {
            case Event:
                EventDefinition event = getEvent(type, row.getCol2());
                if (event.getOtherMethods() == null)
                    event.setOtherMethods(new ArrayList<>());

                event.addOtherMethod(method);
                break;

            case Property:
                PropertyDefinition property = getProperty(type, row.getCol2());
                if (property.getOtherMethods() == null)
                    property.setOtherMethods(new ArrayList<>());

                property.addOtherMethod(method);
                break;

            default:
                throw new UnsupportedOperationException();
        }
    }

    private static EventDefinition getEvent(TypeDefinition type, MetadataToken token) {
        if (token.getTokenType() != TokenType.Event)
            throw new IllegalArgumentException();

        return getMember(type.getEvents(), token);
    }

    private static PropertyDefinition getProperty(TypeDefinition type, MetadataToken token) {
        if (token.getTokenType() != TokenType.Property)
            throw new IllegalArgumentException();

        return getMember(type.getProperties(), token);
    }

    private static <T extends IMemberDefinition> T getMember(Collection<T> members, MetadataToken token) {
        for (T member : members) {
            if (member.getMetadataToken().equals(token))
                return member;
        }

        throw new IllegalArgumentException();
    }

    void initializeMethodSemantics() {
        if (metadata.hasSemanticsRows())
            return;
        int length = moveTo(Table.MethodSemantics);

        metadata.setSemanticsRows(length);

        for (int i = 0; i < length; i++) {
            int attributes = readUInt16();
            int method_rid = readTableIndex(Table.Method);
            MetadataToken association = readMetadataToken(CodedIndex.HasSemantics);

            metadata.setSemanticsRow(method_rid, new Row2<>(attributes, association));
        }
    }

    public PropertyDefinition readMethods(PropertyDefinition property) {
        readAllSemantics(property.getDeclaringType());
        return property;
    }

    public EventDefinition readMethods(EventDefinition event) {
        readAllSemantics(event.getDeclaringType());
        return event;
    }

    public int readAllSemantics(MethodDefinition method) {
        readAllSemantics(method.getDeclaringType());

        return method.getSemAttrs();
    }

    void readAllSemantics(TypeDefinition type) {
        Collection<MethodDefinition> methods = type.getMethods();
        for (MethodDefinition method : methods) {
            if (method.isSemAttrsReady())
                continue;

            method.setSemAttrs(ReadMethodSemantics(method));
            method.setSemAttrsReady(true);
        }
    }

    Range readParametersRange(int method_rid) {
        return readListRange(method_rid, Table.Method, Table.Param);
    }

    public Collection<MethodDefinition> readMethods(TypeDefinition type) {
        Range methodsRange = type.getMethodsRange();
        if (methodsRange.getLength() == 0)
            return new MemberDefinitionCollection<>(type);

        Collection<MethodDefinition> methods = new MemberDefinitionCollection<>(methodsRange.getLength(), type);
        if (moveTo(Table.MethodPtr, methodsRange.getStart()))
            readPointers(Table.MethodPtr, Table.Method, methodsRange, methods, this::readMethod);
        else {
            if (!moveTo(Table.Method, methodsRange.getStart()))
                return methods;

            for (int i = 0; i < methodsRange.getLength(); i++)
                readMethod(methodsRange.getStart() + i, methods);
        }

        return methods;
    }

    private <T extends IMemberDefinition>
    void readPointers(Table ptr, Table table, Range range, Collection<T> members, Action<Integer, Collection<T>> reader) {
        for (int i = 0; i < range.getLength(); i++) {
            moveTo(ptr, range.getStart() + i);

            int rid = readTableIndex(table);
            moveTo(table, rid);

            reader.invoke(rid, members);
        }
    }

    private static boolean isDeleted(IMemberDefinition member) {
        return member.isSpecialName() && member.getName().equals("_Deleted");
    }

    void initializeMethods() {
        if (metadata.hasMethodDefinitions())
            return;

        metadata.setMethodDefinitions(image.getTableLength(Table.Method));
    }

    private void readMethod(int method_rid, Collection<MethodDefinition> methods) {
        MethodDefinition method = new MethodDefinition();
        method.setRva(readUInt32());
        method.setImplAttributes(readUInt16());
        method.setAttributes(readUInt16());
        method.setName(readString());
        method.setMetadataToken(new MetadataToken(TokenType.Method, method_rid));

        if (isDeleted(method))
            return;

        methods.add(method); // attach method

        int signature = readBlobIndex();
        Range param_range = readParametersRange(method_rid);

        context = method;

        readMethodSignature(signature, method);
        metadata.addMethodDefinition(method);

        if (param_range.getLength() == 0)
            return;

        int position = position();
        readParameters(method, param_range);
        offset(position);
    }

    private void readParameters(MethodDefinition method, Range param_range) {
        if (moveTo(Table.ParamPtr, param_range.getStart()))
            readParameterPointers(method, param_range);
        else {
            if (!moveTo(Table.Param, param_range.getStart()))
                return;

            for (int i = 0; i < param_range.getLength(); i++)
                readParameter(param_range.getStart() + i, method);
        }
    }

    private void readParameterPointers(MethodDefinition method, Range range) {
        for (int i = 0; i < range.getLength(); i++) {
            moveTo(Table.ParamPtr, range.getStart() + i);

            int rid = readTableIndex(Table.Param);

            moveTo(Table.Param, rid);

            readParameter(rid, method);
        }
    }

    private void readParameter(int param_rid, MethodDefinition method) {
        int attributes = readUInt16();
        int sequence = readUInt16();
        String name = readString();

        ParameterDefinition parameter = sequence == 0
                ? method.getMethodReturnType().getParameter()
                : method.getParameter(sequence - 1);

        parameter.setMetadataToken(new MetadataToken(TokenType.Param, param_rid));
        parameter.setName(name);
        parameter.setAttributes(attributes);
    }

    private void readMethodSignature(int signature, IMethodSignature method) {
        SignatureReader reader = readSignature(signature);
        reader.readMethodSignature(method);
    }

    public PInvokeInfo readPInvokeInfo(MethodDefinition method) {
        initializePInvokes();
        int rid = method.getMetadataToken().getRid();
        Row3<Integer, Integer, Integer> row = metadata.getPInvokeRow(rid);


        if (row == null)
            return null;

        metadata.removePInvokeRow(rid);

        return new PInvokeInfo(
                row.getCol1(),
                image.getStringHeap().read(row.getCol2()),
                module.getModuleReferences().get(row.getCol3() - 1));
    }

    void initializePInvokes() {
        if (metadata.hasPInvokeRows())
            return;

        int length = moveTo(Table.ImplMap);

        metadata.setPInvokeRows(length);

        for (int i = 1; i <= length; i++) {
            int attributes = readUInt16();
            MetadataToken method = readMetadataToken(CodedIndex.MemberForwarded);
            int name = readStringIndex();
            int scope = readTableIndex(Table.File);

            if (method.getTokenType() != TokenType.Method)
                continue;

            metadata.setpInvokeRow(method.getRid(), new Row3<>(attributes, name, scope));
        }
    }

    public boolean hasGenericParameters(IGenericParameterProvider provider) {
        initializeGenericParameters();
        Range[] ranges = metadata.getGenericParameterRanges(provider);
        return ranges != null && rangesSize(ranges) > 0;
    }

    public List<GenericParameter> readGenericParameters(IGenericParameterProvider provider) {
        initializeGenericParameters();

        Range[] ranges = metadata.getGenericParameterRanges(provider);
        if (ranges == null)
            return new GenericParameterCollection(provider);

        metadata.removeGenericParameterRanges(provider);

        GenericParameterCollection genericParameters = new GenericParameterCollection(rangesSize(ranges), provider);

        for (Range range : ranges)
            readGenericParametersRange(range, provider, genericParameters);

        return genericParameters;
    }

    private void readGenericParametersRange(Range range, IGenericParameterProvider provider, GenericParameterCollection genericParameters) {
        if (!moveTo(Table.GenericParam, range.getStart()))
            return;

        for (int i = 0; i < range.getLength(); i++) {
            readUInt16(); // index
            int flags = readUInt16();
            readMetadataToken(CodedIndex.TypeOrMethodDef);
            String name = readString();

            GenericParameter parameter = new GenericParameter(name, provider);
            parameter.setMetadataToken(new MetadataToken(TokenType.GenericParam, range.getStart() + i));
            parameter.setAttributes(flags);

            genericParameters.add(parameter);
        }
    }

    void initializeGenericParameters() {
        if (metadata.hasGenericParameterRanges())
            return;

        Map<MetadataToken, Range[]> map = initializeRanges(
                Table.GenericParam,
                (reader, item) -> {
                    reader.advance(4);
                    MetadataToken next = reader.readMetadataToken(CodedIndex.TypeOrMethodDef);
                    reader.readStringIndex();
                    return next;
                });

        metadata.setGenericParameterRanges(map);
    }

    private Map<MetadataToken, Range[]> initializeRanges(Table table, Callback<MetadataToken, Void> nextCallback) {
        int length = moveTo(table);
        Map<MetadataToken, Range[]> ranges = new HashMap<>(length);

        if (length == 0)
            return ranges;

        MetadataToken owner = MetadataToken.ZERO;
        Range range = new Range(1, 0);

        for (int i = 1; i <= length; i++) {
            MetadataToken next = nextCallback.invoke(this, null);

            if (i == 1) {
                owner = next;
                range.setLength(range.getLength() + 1);
            } else //noinspection ObjectEquality
                if (next != owner) {
                    addRange(ranges, owner, range);
                    range = new Range(i, 1);
                    owner = next;
                } else
                    range.setLength(range.getLength() + 1);
        }

        addRange(ranges, owner, range);

        return ranges;
    }

    private static void addRange(Map<MetadataToken, Range[]> ranges, MetadataToken owner, Range range) {
        if (owner.getRid() == 0)
            return;

        ranges.put(owner, ArrayUtils.addAll(ranges.get(owner), range));
    }

    public Boolean hasGenericConstraints(GenericParameter genericParameter) {
        initializeGenericConstraints();
        MetadataToken[] mapping = metadata.getGenericConstraints(genericParameter);
        return mapping != null && mapping.length > 0;
    }

    public Collection<TypeReference> readGenericConstraints(GenericParameter genericParameter) {
        initializeGenericConstraints();

        MetadataToken[] tokens = metadata.getGenericConstraints(genericParameter);
        if (tokens == null)
            return Collections.emptyList();

        Collection<TypeReference> constraints = new ArrayList<>(tokens.length);

        context = (IGenericContext) genericParameter.getOwner();

        for (MetadataToken token : tokens)
            constraints.add(getTypeDefOrRef(token));

        metadata.removeGenericConstraints(genericParameter);

        return constraints;
    }

    void initializeGenericConstraints() {
        if (metadata.hasGenericConstraints())
            return;

        int length = moveTo(Table.GenericParamConstraint);

        metadata.setGenericConstraints(length);

        for (int i = 1; i <= length; i++)
            metadata.addGenericConstraints(
                    readTableIndex(Table.GenericParam),
                    readMetadataToken(CodedIndex.TypeDefOrRef));
    }

    public boolean hasOverrides(MethodDefinition method) {
        InitializeOverrides();
        MetadataToken[] mapping = metadata.getOverrides(method);
        return mapping != null && mapping.length > 0;
    }

    public Collection<MethodReference> readOverrides(MethodDefinition method) {
        InitializeOverrides();

        MetadataToken[] tokens = metadata.getOverrides(method);
        if (tokens == null)
            return Collections.emptyList();

        Collection<MethodReference> overrides = new ArrayList<>(tokens.length);

        context = method;

        for (MetadataToken token : tokens)
            overrides.add((MethodReference) lookupToken(token));

        metadata.removeOverrides(method);
        return overrides;
    }

    void InitializeOverrides() {
        if (metadata.hasOverrides())
            return;

        int length = moveTo(Table.MethodImpl);

        metadata.setOverrides(length);

        for (int i = 1; i <= length; i++) {
            readTableIndex(Table.TypeDef);

            MetadataToken method = readMetadataToken(CodedIndex.MethodDefOrRef);
            if (method.getTokenType() != TokenType.Method)
                throw new UnsupportedOperationException();

            MetadataToken override = readMetadataToken(CodedIndex.MethodDefOrRef);

            metadata.addOverrides(method.getRid(), override);
        }
    }

//	public MethodBody readMethodBody( MethodDefinition method )
//	{
//		return code.ReadMethodBody( method );
//	}
//
//	public CallSite ReadCallSite( MetadataToken token )
//	{
//		if( !MoveTo( Table.StandAloneSig, token.RID ) )
//			return null;
//
//		var signature = readBlobIndex();
//
//		var call_site = new CallSite();
//
//		readMethodSignature( signature, call_site );
//
//		call_site.MetadataToken = token;
//
//		return call_site;
//	}
//
//	public VariableDefinitionCollection ReadVariables( MetadataToken local_var_token )
//	{
//		if( !MoveTo( Table.StandAloneSig, local_var_token.RID ) )
//			return null;
//
//		var reader = readSignature( readBlobIndex() );
//		const byte local_sig = 0x7;
//
//		if( reader.readByte() != local_sig )
//			throw new NotSupportedException();
//
//		var count = reader.ReadCompressedUInt32();
//		if( count == 0 )
//			return null;
//
//		var variables = new VariableDefinitionCollection( (int)count );
//
//		for( int i = 0; i < count; i++ )
//			variables.Add( new VariableDefinition( reader.ReadTypeSignature() ) );
//
//		return variables;
//	}

    public IMetadataTokenProvider lookupToken(MetadataToken token) {
        int rid = token.getRid();

        if (rid == 0)
            return null;

        IMetadataTokenProvider element;
        int position = position();
        IGenericContext genericContext = context;

        switch (token.getTokenType()) {
            case TypeDef:
                element = getTypeDefinition(rid);
                break;
            case TypeRef:
                element = getTypeReference(rid);
                break;
            case TypeSpec:
                element = getTypeSpecification(rid);
                break;
            case Field:
                element = getFieldDefinition(rid);
                break;
            case Method:
                element = getMethodDefinition(rid);
                break;
            case MemberRef:
                element = getMemberReference(rid);
                break;
            case MethodSpec:
                element = getMethodSpecification(rid);
                break;
            default:
                element = null;
        }

        offset(position);
        context = genericContext;

        return element;
    }

    @Nullable
    public FieldDefinition getFieldDefinition(int rid) {
        initializeTypeDefinitions();

        FieldDefinition field = metadata.getFieldDefinition(rid);
        if (field != null)
            return field;

        return lookupField(rid);
    }

    @Nullable
    private FieldDefinition lookupField(int rid) {
        TypeDefinition type = metadata.getFieldDeclaringType(rid);
        if (type == null)
            return null;

        initializeCollection(type.getFields());

        return metadata.getFieldDefinition(rid);
    }

    public MethodDefinition getMethodDefinition(int rid) {
        initializeTypeDefinitions();

        MethodDefinition method = metadata.getMethodDefinition(rid);
        if (method != null)
            return method;

        return lookupMethod(rid);
    }

    private MethodDefinition lookupMethod(int rid) {
        TypeDefinition type = metadata.getMethodDeclaringType(rid);
        if (type == null)
            return null;

        initializeCollection(type.getMethods());

        return metadata.getMethodDefinition(rid);
    }

    private MethodSpecification getMethodSpecification(int rid) {
        if (!moveTo(Table.MethodSpec, rid))
            return null;

        MethodReference element_method = (MethodReference) lookupToken(readMetadataToken(CodedIndex.MethodDefOrRef));
        int signature = readBlobIndex();

        MethodSpecification method_spec = readMethodSpecSignature(signature, element_method);
        method_spec.setMetadataToken(new MetadataToken(TokenType.MethodSpec, rid));
        return method_spec;
    }

    private MethodSpecification readMethodSpecSignature(int signature, MethodReference method) {
        SignatureReader reader = readSignature(signature);
        int methodspec_sig = 0x0a;

        int call_conv = reader.readByte();

        if (call_conv != methodspec_sig)
            throw new UnsupportedOperationException();

        GenericInstanceMethod instance = new GenericInstanceMethod(method);

        reader.readGenericInstanceSignature(method, instance);

        return instance;
    }

    private MemberReference getMemberReference(int rid) {
        InitializeMemberReferences();

        MemberReference member = metadata.getMemberReference(rid);
        if (member != null)
            return member;

        member = readMemberReference(rid);
        if (member != null && !member.containsGenericParameter())
            metadata.addMemberReference(member);
        return member;
    }

    private MemberReference readMemberReference(int rid) {
        if (!moveTo(Table.MemberRef, rid))
            return null;

        MetadataToken token = readMetadataToken(CodedIndex.MemberRefParent);
        String name = readString();
        int signature = readBlobIndex();

        MemberReference member;

        switch (token.getTokenType()) {
            case TypeDef:
            case TypeRef:
            case TypeSpec:
                member = readTypeMemberReference(token, name, signature);
                break;
            case Method:
                member = readMethodMemberReference(token, name, signature);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        member.setMetadataToken(new MetadataToken(TokenType.MemberRef, rid));

        return member;
    }

    private MemberReference readTypeMemberReference(MetadataToken type, String name, int signature) {
        TypeReference declaring_type = getTypeDefOrRef(type);

        if (!declaring_type.isArray())
            context = declaring_type;

        MemberReference member = readMemberReferenceSignature(signature, declaring_type);
        member.setName(name);

        return member;
    }

    private MemberReference readMemberReferenceSignature(int signature, TypeReference declaring_type) {
        SignatureReader reader = readSignature(signature);
        int field_sig = 0x6;

        if (reader.bytes()[reader.position()] == field_sig) {
            reader.advance(1);
            FieldReference field = new FieldReference();
            field.setDeclaringType(declaring_type);
            field.setFieldType(reader.readTypeSignature());
            return field;
        } else {
            MethodReference method = new MethodReference();
            method.setDeclaringType(declaring_type);
            reader.readMethodSignature(method);
            return method;
        }
    }

    private MemberReference readMethodMemberReference(MetadataToken token, String name, int signature) {
        MethodDefinition method = getMethodDefinition(token.getRid());

        context = method;

        MemberReference member = readMemberReferenceSignature(signature, method.getDeclaringType());
        member.setName(name);

        return member;
    }

    private void InitializeMemberReferences() {
        if (metadata.hasMemberReferences())
            return;

        metadata.setMemberReferences(image.getTableLength(Table.MemberRef));
    }

    public Collection<MemberReference> getMemberReferences() {
        InitializeMemberReferences();

        int length = image.getTableLength(Table.MemberRef);

        TypeSystem typeSystem = module.getTypeSystem();

        MethodReference methodReference = new MethodReference(null, typeSystem.getType_void());
        methodReference.setDeclaringType(new TypeReference(null, null, module, typeSystem.corlib()));

        Collection<MemberReference> references = new ArrayList<>();

        for (int i = 1; i <= length; i++) {
            context = methodReference;
            references.add(getMemberReference(i));
        }

        return references;
    }

    void initializeConstants() {
        if (metadata.hasConstantRows())
            return;

        int length = moveTo(Table.Constant);

        metadata.setConstantRows(length);

        for (int i = 1; i <= length; i++) {
            ElementType type = ElementType.byCode(readUInt16());
            MetadataToken owner = readMetadataToken(CodedIndex.HasConstant);
            int signature = readBlobIndex();

            metadata.setConstantRow(owner, new Row2<>(type, signature));
        }
    }

    public Object readConstant(IConstantProvider owner) {
        initializeConstants();

        Row2<ElementType, Integer> row = metadata.getConstantRow(owner.getMetadataToken());
        if (row == null)
            return Utils.NO_VALUE;

        metadata.removeConstantRow(owner.getMetadataToken());

        switch (row.getCol1()) {
            case Class:
            case Object:
                return null;

            case String:
                return readConstantString(readBlob(row.getCol2()));
            default:
                return ReadConstantPrimitive(row.getCol1(), row.getCol2());
        }
    }

    private static String readConstantString(byte[] blob) {
        int length = blob.length;
        if ((length & 1) == 1)
            length--;

        try {
            return new String(blob, 0, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Object ReadConstantPrimitive(ElementType type, int signature) {
        SignatureReader reader = readSignature(signature);
        return reader.readConstantSignature(type);
    }

    void initializeCustomAttributes() {
        if (metadata.hasCustomAttributeRanges())
            return;

        Map<MetadataToken, Range[]> map = initializeRanges(
                Table.CustomAttribute,
                (reader, item) -> {
                    MetadataToken next = readMetadataToken(CodedIndex.HasCustomAttribute);
                    readMetadataToken(CodedIndex.CustomAttributeType);
                    readBlobIndex();
                    return next;
                });

        metadata.setCustomAttributeRanges(map);
    }

    public Boolean hasCustomAttributes(ICustomAttributeProvider owner) {
        initializeCustomAttributes();

        Range[] ranges = metadata.getCustomAttributeRanges(owner);
        return ranges != null && rangesSize(ranges) > 0;
    }

    public Collection<CustomAttribute> readCustomAttributes(ICustomAttributeProvider owner) {
        initializeCustomAttributes();

        Range[] ranges = metadata.getCustomAttributeRanges(owner);
        if (ranges == null)
            return Collections.emptyList();

        Collection<CustomAttribute> custom_attributes = new ArrayList<>(rangesSize(ranges));

        for (Range range : ranges)
            readCustomAttributeRange(range, custom_attributes);

        metadata.removeCustomAttributeRanges(owner);

        return custom_attributes;
    }

    private void readCustomAttributeRange(Range range, Collection<CustomAttribute> custom_attributes) {
        if (!moveTo(Table.CustomAttribute, range.getStart()))
            return;

        for (int i = 0; i < range.getLength(); i++) {
            readMetadataToken(CodedIndex.HasCustomAttribute);

            MethodReference constructor = (MethodReference) lookupToken(readMetadataToken(CodedIndex.CustomAttributeType));

            int signature = readBlobIndex();

            //noinspection ConstantConditions
            custom_attributes.add(new CustomAttribute(signature, constructor));
        }
    }

    static int rangesSize(Range[] ranges) {
        int size = 0;
        for (Range range : ranges)
            size += range.getLength();

        return size;
    }

    public byte[] readCustomAttributeBlob(int signature) {
        return readBlob(signature);
    }

    public void readCustomAttributeSignature(CustomAttribute attribute) {
        SignatureReader reader = readSignature(attribute.getSignature());

        if (!reader.canReadMore())
            return;

        if (reader.readUInt16() != 0x0001)
            throw new UnsupportedOperationException();

        MethodReference constructor = attribute.getConstructor();
        if (constructor.hasParameters())
            reader.readCustomAttributeConstructorArguments(attribute, constructor.getParameters());

        if (!reader.canReadMore())
            return;

        int named = reader.readUInt16();

        if (named == 0)
            return;

        reader.readCustomAttributeNamedArguments(named, attribute.getFields(), attribute.getProperties());
    }

    private void initializeMarshalInfos() {
        if (metadata.hasFieldMarshals())
            return;

        int length = moveTo(Table.FieldMarshal);

        metadata.setFieldMarshals(length);

        for (int i = 0; i < length; i++) {
            MetadataToken token = readMetadataToken(CodedIndex.HasFieldMarshal);
            int signature = readBlobIndex();
            if (token.getRid() == 0)
                continue;

            metadata.setFieldMarshal(token, signature);
        }
    }

    public boolean hasMarshalInfo(IMarshalInfoProvider owner) {
        initializeMarshalInfos();

        return metadata.getFieldMarshal(owner.getMetadataToken()) != null;
    }

    @Nullable
    public MarshalInfo readMarshalInfo(IMarshalInfoProvider owner) {
        initializeMarshalInfos();

        Integer signature = metadata.getFieldMarshal(owner.getMetadataToken());
        if (signature == null)
            return null;

        SignatureReader reader = readSignature(signature);

        metadata.removeFieldMarshal(owner.getMetadataToken());

        return reader.readMarshalInfo();
    }

    void InitializeSecurityDeclarations() {
        if (metadata.hasSecurityDeclarationRanges())
            return;

        Map<MetadataToken, Range[]> map = initializeRanges(
                Table.DeclSecurity,
                (reader, item) -> {
                    readUInt16();
                    MetadataToken next = readMetadataToken(CodedIndex.HasDeclSecurity);
                    readBlobIndex();
                    return next;
                });

        metadata.setSecurityDeclarationRanges(map);
    }

    public boolean hasSecurityDeclarations(ISecurityDeclarationProvider owner) {
        InitializeSecurityDeclarations();

        Range[] ranges = metadata.getSecurityDeclarationRanges(owner);
        return ranges != null && rangesSize(ranges) > 0;

    }

    public Collection<SecurityDeclaration> readSecurityDeclarations(ISecurityDeclarationProvider owner) {
        InitializeSecurityDeclarations();

        Range[] ranges = metadata.getSecurityDeclarationRanges(owner);
        if (ranges == null)
            return Collections.emptyList();

        Collection<SecurityDeclaration> securityDeclarations = new ArrayList<>(rangesSize(ranges));

        for (Range range : ranges)
            ReadSecurityDeclarationRange(range, securityDeclarations);

        metadata.removeSecurityDeclarationRanges(owner);

        return securityDeclarations;
    }

    private void ReadSecurityDeclarationRange(Range range, Collection<SecurityDeclaration> securityDeclarations) {
        if (!moveTo(Table.DeclSecurity, range.getStart()))
            return;

        for (int i = 0; i < range.getLength(); i++) {
            SecurityAction action = SecurityAction.values()[readUInt16()];
            readMetadataToken(CodedIndex.HasDeclSecurity);
            int signature = readBlobIndex();

            securityDeclarations.add(new SecurityDeclaration(action, signature, module));
        }
    }

    public byte[] readSecurityDeclarationBlob(int signature) {
        return readBlob(signature);
    }

    public void readSecurityDeclarationSignature(SecurityDeclaration declaration) {
        int signature = declaration.getSignature();
        SignatureReader reader = readSignature(signature);

        if (reader.bytes()[reader.position()] != '.') {
            readXmlSecurityDeclaration(signature, declaration);
            return;
        }

        reader.advance(1);
        int count = reader.readCompressedUInt32();
        Collection<SecurityAttribute> attributes = new ArrayList<>(count);

        for (int i = 0; i < count; i++)
            attributes.add(reader.readSecurityAttribute());

        declaration.setSecurityAttributes(attributes);
    }

    void readXmlSecurityDeclaration(int signature, SecurityDeclaration declaration) {
        byte[] blob = readBlob(signature);

        SecurityAttribute attribute = new SecurityAttribute(module.getTypeSystem().lookupType("System.Security.Permissions", "PermissionSetAttribute"));

        attribute.setProperties(new ArrayList<>(1));

        String value;
        try {
            value = new String(blob, 0, blob.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        attribute.addProperty(
                new CustomAttributeNamedArgument(
                        "XML",
                        new CustomAttributeArgument(
                                module.getTypeSystem().getType_string(),
                                value)));

        Collection<SecurityAttribute> attributes = new ArrayList<>(1);
        attributes.add(attribute);

        declaration.setSecurityAttributes(attributes);
    }

    public Collection<ExportedType> readExportedTypes() {
        int length = moveTo(Table.ExportedType);
        if (length == 0)
            return Collections.emptyList();

        List<ExportedType> exportedTypes = new ArrayList<>();

        for (int i = 1; i <= length; i++) {
            int attributes = readUInt32();
            int identifier = readUInt32();
            String name = readString();
            String namespace = readString();
            MetadataToken implementation = readMetadataToken(CodedIndex.Implementation);

            ExportedType declaring_type = null;
            IMetadataScope scope = null;

            switch (implementation.getTokenType()) {
                case AssemblyRef:
                case File:
                    scope = getExportedTypeScope(implementation);
                    break;
                case ExportedType:
                    // FIXME: if the table is not properly sorted
                    declaring_type = exportedTypes.get(implementation.getRid() - 1);
                    break;

                default:
            }

            ExportedType exportedType = new ExportedType(namespace, name, module, scope);
            exportedType.setAttributes(attributes);
            exportedType.setIdentifier(identifier);
            //noinspection ConstantConditions
            exportedType.setDeclaringType(declaring_type);
            exportedType.setMetadataToken(new MetadataToken(TokenType.ExportedType, i));

            exportedTypes.add(exportedType);
        }

        return exportedTypes;
    }

    @Nullable
    private IMetadataScope getExportedTypeScope(MetadataToken token) {
        int position = position();
        IMetadataScope scope;

        switch (token.getTokenType()) {
            case AssemblyRef:
                initializeAssemblyReferences();
                scope = metadata.getAssemblyNameReference(token.getRid());
                break;
            case File:
                initializeModuleReferences();
                scope = getModuleReferenceFromFile(token);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        offset(position);
        return scope;
    }

    @Nullable
    private ModuleReference getModuleReferenceFromFile(MetadataToken token) {
        if (!moveTo(Table.File, token.getRid()))
            return null;

        readUInt32();
        String file_name = readString();
        Collection<ModuleReference> modules = module.getModuleReferences();

        for (ModuleReference reference : modules)
            if (reference.getName().equals(file_name))
                return reference;


        ModuleReference reference = new ModuleReference(file_name);
        modules.add(reference);
        return reference;
    }

    @SuppressWarnings("EmptyMethod")
    private static void initializeCollection(@SuppressWarnings("UnusedParameters") Object o) {
    }


}
