package mono.cecil;

import mono.cecil.metadata.MetadataToken;
import mono.cecil.metadata.rows.Row2;
import mono.cecil.metadata.rows.Row3;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "TypeMayBeWeakened"})
public final class MetadataSystem implements IDisposable {
    private AssemblyNameReference[] assemblyReferences;
    private ModuleReference[] moduleReferences;

    private TypeDefinition[] types;
    private TypeReference[] typeReferences;

    private FieldDefinition[] fields;
    private MethodDefinition[] methods;
    private MemberReference[] memberReferences;

    private Map<Integer, Integer[]> nestedTypes;
    private Map<Integer, Integer> reverseNestedTypes;
    private Map<Integer, MetadataToken[]> interfaces;
    private Map<Integer, Row2<Integer, Integer>> classLayouts;
    private Map<Integer, Integer> fieldLayouts;
    private Map<Integer, Integer> fieldRvas;
    private Map<MetadataToken, Integer> fieldMarshals;
    private Map<MetadataToken, Row2<ElementType, Integer>> constants;
    private Map<Integer, MetadataToken[]> overrides;
    private Map<MetadataToken, Range[]> customAttributes;
    private Map<MetadataToken, Range[]> securityDeclarations;
    private Map<Integer, Range> events;
    private Map<Integer, Range> properties;
    private Map<Integer, Row2<Integer, MetadataToken>> semantics;
    private Map<Integer, Row3<Integer, Integer, Integer>> pInvokes;
    private Map<MetadataToken, Range[]> genericParameters;
    private Map<Integer, MetadataToken[]> genericConstraints;

    @Override
    public void dispose() {
        assemblyReferences = null;
        moduleReferences = null;
        types = null;
        typeReferences = null;
        fields = null;
        methods = null;
        memberReferences = null;

        if (nestedTypes != null) nestedTypes.clear();
        if (reverseNestedTypes != null) reverseNestedTypes.clear();
        if (interfaces != null) interfaces.clear();
        if (classLayouts != null) classLayouts.clear();
        if (fieldLayouts != null) fieldLayouts.clear();
        if (fieldRvas != null) fieldRvas.clear();
        if (fieldMarshals != null) fieldMarshals.clear();
        if (constants != null) constants.clear();
        if (overrides != null) overrides.clear();
        if (customAttributes != null) customAttributes.clear();
        if (securityDeclarations != null) securityDeclarations.clear();
        if (events != null) events.clear();
        if (properties != null) properties.clear();
        if (semantics != null) semantics.clear();
        if (pInvokes != null) pInvokes.clear();
        if (genericParameters != null) genericParameters.clear();
        if (genericConstraints != null) genericConstraints.clear();
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public boolean hasAssemblyReferences() {
        return assemblyReferences != null;
    }

    public void setAssemblyReferences(int size) {
        assemblyReferences = new AssemblyNameReference[size];
    }

    public AssemblyNameReference getAssemblyNameReference(int rid) {
        return assemblyReferences[rid - 1];
    }

    public void setAssemblyReference(int index, AssemblyNameReference reference) {
        assemblyReferences[index] = reference;
    }

    public List<AssemblyNameReference> getAssemblyReferences() {
        return Arrays.asList(assemblyReferences);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public boolean hasModuleReferences() {
        return moduleReferences != null;
    }

    public void setModuleReferences(int size) {
        moduleReferences = new ModuleReference[size];
    }

    public void setModuleReference(int index, ModuleReference reference) {
        moduleReferences[index] = reference;
    }

    public List<ModuleReference> getModuleReferences() {
        return Arrays.asList(moduleReferences);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    @Nullable
    public TypeDefinition getTypeDefinition(int rid) {
        if (rid < 1 || rid > types.length)
            return null;

        return types[rid - 1];
    }

    public void addTypeDefinition(TypeDefinition type) {
        types[type.getMetadataToken().getRid() - 1] = type;
    }

    public boolean hasTypeDefinitions() {
        return types != null;
    }

    public void setTypeDefinitions(int size) {
        types = new TypeDefinition[size];
    }

    public TypeDefinition[] getTypes() {
        //noinspection ReturnOfCollectionOrArrayField
        return types;
    }

    // -------------------------------------------------------------------------------------------------------------- //
    @Nullable
    public TypeReference getTypeReference(int rid) {
        if (rid < 1 || rid > typeReferences.length)
            return null;

        return typeReferences[rid - 1];
    }

    public void addTypeReference(TypeReference reference) {
        typeReferences[reference.getMetadataToken().getRid() - 1] = reference;
    }

    public boolean hasTypeReferences() {
        return typeReferences != null;
    }

    public void setTypeReferences(int size) {
        typeReferences = new TypeReference[size];
    }

    public int getTypeReferencesLength() {
        return ArrayUtils.getLength(typeReferences);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    @Nullable
    public FieldDefinition getFieldDefinition(int rid) {
        if (rid < 1 || rid > fields.length)
            return null;

        return fields[rid - 1];
    }

    public void addFieldDefinition(FieldDefinition field) {
        fields[field.getMetadataToken().getRid() - 1] = field;
    }

    public boolean hasFieldDefinitions() {
        return fields != null;
    }

    public void setFieldDefinitions(int size) {
        fields = new FieldDefinition[size];
    }

    // -------------------------------------------------------------------------------------------------------------- //
    @Nullable
    public MethodDefinition getMethodDefinition(int rid) {
        if (rid < 1 || rid > methods.length)
            return null;

        return methods[rid - 1];
    }

    public void addMethodDefinition(MethodDefinition method) {
        methods[method.getMetadataToken().getRid() - 1] = method;
    }

    public boolean hasMethodDefinitions() {
        return methods != null;
    }

    public void setMethodDefinitions(int size) {
        methods = new MethodDefinition[size];
    }

    // -------------------------------------------------------------------------------------------------------------- //
    @Nullable
    public MemberReference getMemberReference(int rid) {
        if (rid < 1 || rid > memberReferences.length)
            return null;

        return memberReferences[rid - 1];
    }

    public void addMemberReference(MemberReference reference) {
        memberReferences[reference.getMetadataToken().getRid() - 1] = reference;
    }

    public boolean hasMemberReferences() {
        return memberReferences != null;
    }

    public void setMemberReferences(int size) {
        memberReferences = new MemberReference[size];
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Integer[] getNestedType(int rid) {
        return nestedTypes.get(rid);
    }

    public void addNestedType(int rid, Integer... values) {
        Integer[] mapping = getNestedType(rid);
        nestedTypes.put(rid, ArrayUtils.addAll(mapping, values));
    }

    public boolean hasNestedTypes() {
        return nestedTypes != null;
    }

    public void setNestedTypes(int size) {
        nestedTypes = new HashMap<>(size);
    }

    public void removeNestedType(int rid) {
        nestedTypes.remove(rid);
    }

    public int getNestedTypesSize() {
        return nestedTypes.size();
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Integer getReverseNestedType(TypeDefinition type) {
        return reverseNestedTypes.get(type.getMetadataToken().getRid());
    }

    public void setReverseNestedType(int nested, int declaring) {
        reverseNestedTypes.put(nested, declaring);
    }

    public void removeReverseNestedType(TypeDefinition type) {
        reverseNestedTypes.remove(type.getMetadataToken().getRid());
    }

    public void setReverseNestedTypes(int size) {
        reverseNestedTypes = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public MetadataToken[] getInterfaces(TypeDefinition type) {
        return interfaces.get(type.getMetadataToken().getRid());
    }

    public void addInterfaces(int rid, MetadataToken... tokens) {
        MetadataToken[] value = ArrayUtils.addAll(interfaces.get(rid), tokens);
        interfaces.put(rid, value);
    }

    public void removeInterfaces(int rid) {
        interfaces.remove(rid);
    }

    public boolean hasInterfaces() {
        return interfaces != null;
    }

    public void setInterfaces(int size) {
        interfaces = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public void addPropertiesRange(int rid, Range range) {
        properties.put(rid, range);
    }

    public Range getPropertiesRange(TypeDefinition type) {
        return properties.get(type.getMetadataToken().getRid());
    }

    public void removePropertiesRange(TypeDefinition type) {
        properties.remove(type.getMetadataToken().getRid());
    }

    public boolean hasProperties() {
        return properties != null;
    }

    public void setProperties(int size) {
        properties = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public void setEventRange(int rid, Range range) {
        events.put(rid, range);
    }

    public Range getEventRange(TypeDefinition type) {
        return events.get(type.getMetadataToken().getRid());
    }

    public void removeEventRange(TypeDefinition type) {
        events.remove(type.getMetadataToken().getRid());
    }

    public boolean hasEventRanges() {
        return events != null;
    }

    public void setEventRanges(int size) {
        events = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Range[] getGenericParameterRanges(IGenericParameterProvider owner) {
        return genericParameters.get(owner.getMetadataToken());
    }

    public void removeGenericParameterRanges(IGenericParameterProvider owner) {
        genericParameters.remove(owner.getMetadataToken());
    }

    public boolean hasGenericParameterRanges() {
        return genericParameters != null;
    }

    public void setGenericParameterRanges(Map<MetadataToken, Range[]> map) {
        //noinspection AssignmentToCollectionOrArrayFieldFromParameter
        genericParameters = map;
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Range[] getCustomAttributeRanges(ICustomAttributeProvider owner) {
        return customAttributes.get(owner.getMetadataToken());
    }

    public void removeCustomAttributeRanges(ICustomAttributeProvider owner) {
        customAttributes.remove(owner.getMetadataToken());
    }

    public boolean hasCustomAttributeRanges() {
        return customAttributes != null;
    }

    public void setCustomAttributeRanges(Map<MetadataToken, Range[]> map) {
        //noinspection AssignmentToCollectionOrArrayFieldFromParameter
        customAttributes = map;
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Range[] getSecurityDeclarationRanges(ISecurityDeclarationProvider owner) {
        return securityDeclarations.get(owner.getMetadataToken());
    }

    public void removeSecurityDeclarationRanges(ISecurityDeclarationProvider owner) {
        securityDeclarations.remove(owner.getMetadataToken());
    }

    public boolean hasSecurityDeclarationRanges() {
        return securityDeclarations != null;
    }

    public void setSecurityDeclarationRanges(Map<MetadataToken, Range[]> map) {
        //noinspection AssignmentToCollectionOrArrayFieldFromParameter
        securityDeclarations = map;
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public MetadataToken[] getGenericConstraints(GenericParameter genericParameter) {
        return genericConstraints.get(genericParameter.getMetadataToken().getRid());
    }

    public void addGenericConstraints(int rid, MetadataToken... tokens) {
        genericConstraints.put(rid, ArrayUtils.addAll(genericConstraints.get(rid), tokens));
    }

    public void removeGenericConstraints(GenericParameter genericParameter) {
        genericConstraints.remove(genericParameter.getMetadataToken().getRid());
    }

    public boolean hasGenericConstraints() {
        return genericConstraints != null;
    }

    public void setGenericConstraints(int size) {
        genericConstraints = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public MetadataToken[] getOverrides(MethodDefinition method) {
        return overrides.get(method.getMetadataToken().getRid());
    }

    public void addOverrides(int rid, MetadataToken... tokens) {
        overrides.put(rid, ArrayUtils.addAll(overrides.get(rid), tokens));
    }

    public void removeOverrides(MethodDefinition method) {
        overrides.remove(method.getMetadataToken().getRid());
    }

    public boolean hasOverrides() {
        return overrides != null;
    }

    public void setOverrides(int size) {
        overrides = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Integer getFieldRva(int rid) {
        return fieldRvas.get(rid);
    }

    public void removeFieldRva(int rid) {
        fieldRvas.remove(rid);
    }

    public boolean hasFieldRvas() {
        return fieldRvas != null;
    }

    public void setFieldRvas(int size) {
        fieldRvas = new HashMap<>(size);
    }

    public void setFieldRva(int rid, int rva) {
        fieldRvas.put(rid, rva);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Integer getFieldLayout(int rid) {
        return fieldLayouts.get(rid);
    }

    public void removeFieldLayout(int rid) {
        fieldLayouts.remove(rid);
    }

    public void setFieldLayout(int rid, int value) {
        fieldLayouts.put(rid, value);
    }

    public boolean hasFieldLayouts() {
        return fieldLayouts != null;
    }

    public void setFieldLayouts(int size) {
        fieldLayouts = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Row2<Integer, MetadataToken> getSemanticsRow(int rid) {
        return semantics.get(rid);
    }

    public void removeSemanticsRow(int rid) {
        semantics.remove(rid);
    }

    public void setSemanticsRow(int rid, Row2<Integer, MetadataToken> row) {
        semantics.put(rid, row);
    }

    public boolean hasSemanticsRows() {
        return semantics != null;
    }

    public void setSemanticsRows(int size) {
        semantics = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Row3<Integer, Integer, Integer> getPInvokeRow(int rid) {
        return pInvokes.get(rid);
    }

    public void removePInvokeRow(int rid) {
        pInvokes.remove(rid);
    }

    public void setpInvokeRow(int rid, Row3<Integer, Integer, Integer> row) {
        pInvokes.put(rid, row);
    }

    public boolean hasPInvokeRows() {
        return pInvokes != null;
    }

    public void setPInvokeRows(int size) {
        pInvokes = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Integer getFieldMarshal(MetadataToken token) {
        return fieldMarshals.get(token);
    }

    public void removeFieldMarshal(MetadataToken token) {
        fieldMarshals.remove(token);
    }

    public void setFieldMarshal(MetadataToken token, Integer value) {
        fieldMarshals.put(token, value);
    }

    public void setFieldMarshals(int size) {
        fieldMarshals = new HashMap<>(size);
    }

    public boolean hasFieldMarshals() {
        return fieldMarshals != null;
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Row2<ElementType, Integer> getConstantRow(MetadataToken token) {
        return constants.get(token);
    }

    public void removeConstantRow(MetadataToken token) {
        constants.remove(token);
    }

    public void setConstantRow(MetadataToken token, Row2<ElementType, Integer> row) {
        constants.put(token, row);
    }

    public boolean hasConstantRows() {
        return constants != null;
    }

    public void setConstantRows(int size) {
        constants = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    public Row2<Integer, Integer> getClassLayoutRow(int rid) {
        return classLayouts.get(rid);
    }

    public void removeClassLayoutRow(int rid) {
        classLayouts.remove(rid);
    }

    public void setClassLayoutRow(int rid, Row2<Integer, Integer> row) {
        classLayouts.put(rid, row);
    }

    public boolean hasClassLayoutRows() {
        return classLayouts != null;
    }

    public void setClassLayoutRows(int size) {
        classLayouts = new HashMap<>(size);
    }

    // -------------------------------------------------------------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------------------------- //
    @Nullable
    public TypeDefinition getFieldDeclaringType(int rid) {
        return binaryRangeSearch(types, rid, true);
    }

    @Nullable
    public TypeDefinition getMethodDeclaringType(int rid) {
        return binaryRangeSearch(types, rid, false);
    }

    // ******************************************** Statics ********************************************************* //
    private static final Map<String, Row2<ElementType, Boolean>> PRIMITIVE_VALUE_TYPES = new HashMap<>();

    static {
        PRIMITIVE_VALUE_TYPES.put("Void", new Row2<>(ElementType.Void, false));
        PRIMITIVE_VALUE_TYPES.put("Boolean", new Row2<>(ElementType.Boolean, true));
        PRIMITIVE_VALUE_TYPES.put("Char", new Row2<>(ElementType.Char, true));
        PRIMITIVE_VALUE_TYPES.put("SByte", new Row2<>(ElementType.I1, true));
        PRIMITIVE_VALUE_TYPES.put("Byte", new Row2<>(ElementType.U1, true));
        PRIMITIVE_VALUE_TYPES.put("Int16", new Row2<>(ElementType.I2, true));
        PRIMITIVE_VALUE_TYPES.put("UInt16", new Row2<>(ElementType.U2, true));
        PRIMITIVE_VALUE_TYPES.put("Int64", new Row2<>(ElementType.I4, true));
        PRIMITIVE_VALUE_TYPES.put("UInt64", new Row2<>(ElementType.U4, true));
        PRIMITIVE_VALUE_TYPES.put("Single", new Row2<>(ElementType.R4, true));
        PRIMITIVE_VALUE_TYPES.put("Double", new Row2<>(ElementType.R8, true));
        PRIMITIVE_VALUE_TYPES.put("String", new Row2<>(ElementType.String, false));
        PRIMITIVE_VALUE_TYPES.put("TypedReference", new Row2<>(ElementType.TypedByRef, false));
        PRIMITIVE_VALUE_TYPES.put("IntPtr", new Row2<>(ElementType.I, true));
        PRIMITIVE_VALUE_TYPES.put("UIntPtr", new Row2<>(ElementType.U, true));
        PRIMITIVE_VALUE_TYPES.put("Object", new Row2<>(ElementType.Object, false));
    }

    private static Row2<ElementType, Boolean> tryGetPrimitiveData(TypeReference type) {
        return PRIMITIVE_VALUE_TYPES.get(type.getName());
    }

    public static void tryProcessPrimitiveTypeReference(TypeReference type) {
        if (!type.getNamespace().equals("System"))
            return;

        IMetadataScope scope = type.getScope();
        if (scope == null || scope.getMetadataScopeType() != MetadataScopeType.AssemblyNameReference)
            return;

        Row2<ElementType, Boolean> primitiveData = tryGetPrimitiveData(type);
        if (primitiveData == null)
            return;

        type.setEtype(primitiveData.getCol1());
        type.setValueType(primitiveData.getCol2());
    }

    public static ElementType tryGetPrimitiveElementType(TypeDefinition type) {
        if (!type.getNamespace().equals("System"))
            return ElementType.None;

        Row2<ElementType, Boolean> primitiveData = tryGetPrimitiveData(type);
        if (primitiveData == null)
            return ElementType.None;

        return primitiveData.getCol1() == null ? ElementType.None : primitiveData.getCol1();
    }

    @Nullable
    private static TypeDefinition binaryRangeSearch(TypeDefinition[] types, int rid, boolean field) {
        int min = 0;
        int max = types.length - 1;
        while (min <= max) {
            int mid = min + ((max - min) / 2);
            TypeDefinition type = types[mid];
            Range range = field ? type.getFieldsRange() : type.getMethodsRange();

            if (rid < range.getStart())
                max = mid - 1;
            else if (rid >= range.getStart() + range.getLength())
                min = mid + 1;
            else
                return type;
        }

        return null;
    }
}
