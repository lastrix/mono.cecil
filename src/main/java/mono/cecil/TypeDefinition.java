package mono.cecil;

import mono.cecil.metadata.MetadataToken;
import mono.cecil.metadata.rows.Row2;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings({"unused", "WeakerAccess", "ReturnOfCollectionOrArrayField", "NestedAssignment", "AssignmentToCollectionOrArrayFieldFromParameter"})
public class TypeDefinition extends TypeReference implements IMemberDefinition, ISecurityDeclarationProvider, IDisposable {
    public TypeDefinition(String namespace, String name, int attributes) {
        super(namespace, name);
        this.attributes = attributes;
        setMetadataToken(new MetadataToken(TokenType.TypeDef));
    }

    public TypeDefinition(String namespace, String name, int attributes, TypeReference baseType) {
        this(namespace, name, attributes);
        this.baseType = baseType;
    }

    private int attributes;
    private TypeReference baseType;
    private Range fieldsRange;
    private Range methodsRange;

    private int packingSize = Utils.NOT_RESOLVED_MARK;
    private int classSize = Utils.NOT_RESOLVED_MARK;

    private Collection<TypeReference> interfaces;
    private Collection<TypeDefinition> nestedTypes;
    private Collection<MethodDefinition> methods;
    private Collection<FieldDefinition> fields;
    private Collection<EventDefinition> events;
    private Collection<PropertyDefinition> properties;
    private Collection<CustomAttribute> customAttributes;
    private Collection<SecurityDeclaration> securityDeclarations;

    public Range getFieldsRange() {
        return fieldsRange;
    }

    public void setFieldsRange(Range fieldsRange) {
        this.fieldsRange = fieldsRange;
    }

    public Range getMethodsRange() {
        return methodsRange;
    }

    public void setMethodsRange(Range methodsRange) {
        this.methodsRange = methodsRange;
    }

    public int getAttributes() {
        return attributes;
    }

    public void setAttributes(int attributes) {
        this.attributes = attributes;
    }

    public TypeReference getBaseType() {
        return baseType;
    }

    public void setBaseType(TypeReference baseType) {
        this.baseType = baseType;
    }

    public boolean hasLayoutInfo() {
        if (packingSize >= 0 || classSize >= 0)
            return true;

        resolveLayout();
        return packingSize >= 0 || classSize >= 0;
    }

    public int getPackingSize() {
        if (packingSize >= 0)
            return packingSize;

        resolveLayout();
        return packingSize >= 0 ? packingSize : -1;
    }

    public void setPackingSize(int packingSize) {
        this.packingSize = packingSize;
    }

    public int getClassSize() {
        if (classSize >= 0)
            return classSize;

        resolveLayout();
        return classSize >= 0 ? classSize : -1;
    }

    public void setClassSize(int classSize) {
        this.classSize = classSize;
    }

    public boolean hasInterfaces() {
        if (interfaces != null)
            return !interfaces.isEmpty();

        if (hasImage())
            return getModule().read(this, MetadataReader::hasInterfaces);

        return false;
    }

    public Collection<TypeReference> getInterfaces() {
        if (interfaces != null)
            return interfaces;

        if (hasImage())
            return interfaces = getModule().read(this, MetadataReader::readInterfaces);

        return interfaces = Collections.emptyList();
    }

    public void setInterfaces(Collection<TypeReference> interfaces) {
        this.interfaces = interfaces;
    }

    public boolean hasNestedTypes() {
        if (nestedTypes != null)
            return !nestedTypes.isEmpty();

        if (hasImage())
            return getModule().read(this, MetadataReader::hasNestedTypes);

        return false;
    }

    public Collection<TypeDefinition> getNestedTypes() {
        if (nestedTypes != null)
            return nestedTypes;

        if (hasImage())
            return nestedTypes = getModule().read(this, MetadataReader::readNestedTypes);
        return nestedTypes = new MemberDefinitionCollection<>(this);
    }

    public void setNestedTypes(Collection<TypeDefinition> nestedTypes) {
        this.nestedTypes = nestedTypes;
    }

    public boolean hasMethods() {
        if (methods != null)
            return !methods.isEmpty();

        //noinspection SimplifiableIfStatement
        if (hasImage())
            return methodsRange.getLength() > 0;

        return false;
    }

    public Collection<MethodDefinition> getMethods() {
        if (methods != null)
            return methods;

        if (hasImage())
            return methods = getModule().read(this, MetadataReader::readMethods);

        return methods = new MemberDefinitionCollection<>(this);
    }

    public void setMethods(Collection<MethodDefinition> methods) {
        this.methods = methods;
    }

    public boolean hasFields() {
        if (fields != null)
            return !fields.isEmpty();

        //noinspection SimplifiableIfStatement
        if (hasImage())
            return fieldsRange.getLength() > 0;

        return false;
    }

    public Collection<FieldDefinition> getFields() {
        if (fields != null)
            return fields;

        if (hasImage())
            return fields = getModule().read(this, MetadataReader::readFields);

        return fields = new MemberDefinitionCollection<>(this);
    }

    public void setFields(Collection<FieldDefinition> fields) {
        this.fields = fields;
    }

    public boolean hasEvents() {
        if (events != null)
            return !events.isEmpty();

        if (hasImage())
            return getModule().read(this, MetadataReader::hasEvents);

        return false;
    }

    public Collection<EventDefinition> getEvents() {
        if (events != null)
            return events;

        if (hasImage())
            return events = getModule().read(this, MetadataReader::readEvents);

        return events = new MemberDefinitionCollection<>(this);
    }

    public void setEvents(Collection<EventDefinition> events) {
        this.events = events;
    }

    public boolean hasProperties() {
        if (properties != null)
            return !properties.isEmpty();

        if (hasImage())
            return getModule().read(this, MetadataReader::hasProperties);

        return false;
    }

    public Collection<PropertyDefinition> getProperties() {
        if (properties != null)
            return properties;

        if (hasImage())
            return properties = getModule().read(this, MetadataReader::readProperties);

        return properties = new MemberDefinitionCollection<>(this);
    }

    public void setProperties(Collection<PropertyDefinition> properties) {
        this.properties = properties;
    }

    @Override
    public boolean hasSecurityDeclarations() {
        if (securityDeclarations != null)
            return !securityDeclarations.isEmpty();

        return Utils.hasSecurityDeclarations(this, getModule());
    }

    @Override
    public Collection<SecurityDeclaration> getSecurityDeclarations() {
        if (securityDeclarations != null)
            return securityDeclarations;
        return securityDeclarations = Utils.getSecurityDeclarations(this, getModule());
    }

    @Override
    public boolean hasCustomAttributes() {
        if (customAttributes != null)
            return !customAttributes.isEmpty();
        return Utils.hasCustomAttributes(this, getModule());
    }

    @Override
    public Collection<CustomAttribute> getCustomAttributes() {
        if (customAttributes != null)
            return customAttributes;
        return customAttributes = Utils.getCustomAttributes(this, getModule());
    }

    @Override
    public boolean hasGenericParameters() {
        if (genericParameters != null)
            return !genericParameters.isEmpty();

        return Utils.hasGenericParameters(this, getModule());
    }

    @Override
    public Collection<GenericParameter> getGenericParameters() {
        if (genericParameters != null)
            return genericParameters;
        setGenericParameters(Utils.getGenericParameters(this, getModule()));
        return genericParameters;
    }

    ////
    public boolean isNotPublic() {
        return TypeAttributes.NotPublic.isSet(attributes);
    }

    public void setNotPublic(boolean value) {
        attributes = TypeAttributes.NotPublic.set(value, attributes);
    }

    public boolean isPublic() {
        return TypeAttributes.Public.isSet(attributes);
    }

    public void setPublic(boolean value) {
        attributes = TypeAttributes.Public.set(value, attributes);
    }

    public boolean isNestedPublic() {
        return TypeAttributes.NestedPublic.isSet(attributes);
    }

    public void setNestedPublic(boolean value) {
        attributes = TypeAttributes.NestedPublic.set(value, attributes);
    }

    public boolean isNestedPrivate() {
        return TypeAttributes.NestedPrivate.isSet(attributes);
    }

    public void setNestedPrivate(boolean value) {
        attributes = TypeAttributes.NestedPrivate.set(value, attributes);
    }

    public boolean isNestedFamily() {
        return TypeAttributes.NestedFamily.isSet(attributes);
    }

    public void setNestedFamily(boolean value) {
        attributes = TypeAttributes.NestedFamily.set(value, attributes);
    }

    public boolean isNestedAssembly() {
        return TypeAttributes.NestedAssembly.isSet(attributes);
    }

    public void setNestedAssembly(boolean value) {
        attributes = TypeAttributes.NestedAssembly.set(value, attributes);
    }

    public boolean isNestedFamilyAndAssembly() {
        return TypeAttributes.NestedFamANDAssem.isSet(attributes);
    }

    public void setNestedFamilyAndAssembly(boolean value) {
        attributes = TypeAttributes.NestedFamANDAssem.set(value, attributes);
    }

    public boolean isNestedFamilyOrAssembly() {
        return TypeAttributes.NestedFamORAssem.isSet(attributes);
    }

    public void setNestedFamilyOrAssembly(boolean value) {
        attributes = TypeAttributes.NestedFamORAssem.set(value, attributes);
    }

    public boolean isAutoLayout() {
        return TypeAttributes.AutoLayout.isSet(attributes);
    }

    public void setAutoLayout(boolean value) {
        attributes = TypeAttributes.AutoLayout.set(value, attributes);
    }

    public boolean isSequentialLayout() {
        return TypeAttributes.SequentialLayout.isSet(attributes);
    }

    public void setSequentialLayout(boolean value) {
        attributes = TypeAttributes.SequentialLayout.set(value, attributes);
    }

    public boolean isExplicitLayout() {
        return TypeAttributes.ExplicitLayout.isSet(attributes);
    }

    public void setExplicitLayout(boolean value) {
        attributes = TypeAttributes.ExplicitLayout.set(value, attributes);
    }

    public boolean isClass() {
        return TypeAttributes.Class.isSet(attributes);
    }

    public void setClass(boolean value) {
        attributes = TypeAttributes.Class.set(value, attributes);
    }

    public boolean isInterface() {
        return TypeAttributes.Interface.isSet(attributes);
    }

    public void setInterface(boolean value) {
        attributes = TypeAttributes.Interface.set(value, attributes);
    }

    public boolean isAbstract() {
        return TypeAttributes.Abstract.isSet(attributes);
    }

    public void setAbstract(boolean value) {
        attributes = TypeAttributes.Abstract.set(value, attributes);
    }

    public boolean isSealed() {
        return TypeAttributes.Sealed.isSet(attributes);
    }

    public void setSealed(boolean value) {
        attributes = TypeAttributes.Sealed.set(value, attributes);
    }

    @Override
    public boolean isSpecialName() {
        return TypeAttributes.SpecialName.isSet(attributes);
    }

    @Override
    public void setSpecialName(boolean value) {
        attributes = TypeAttributes.SpecialName.set(value, attributes);
    }

    public boolean isImport() {
        return TypeAttributes.Import.isSet(attributes);
    }

    public void setImport(boolean value) {
        attributes = TypeAttributes.Import.set(value, attributes);
    }

    public boolean isSerializable() {
        return TypeAttributes.Serializable.isSet(attributes);
    }

    public void setSerializable(boolean value) {
        attributes = TypeAttributes.Serializable.set(value, attributes);
    }

    public boolean isWindowsRuntime() {
        return TypeAttributes.WindowsRuntime.isSet(attributes);
    }

    public void setWindowsRuntime(boolean value) {
        attributes = TypeAttributes.WindowsRuntime.set(value, attributes);
    }

    public boolean isAnsiClass() {
        return TypeAttributes.AnsiClass.isSet(attributes);
    }

    public void setAnsiClass(boolean value) {
        attributes = TypeAttributes.AnsiClass.set(value, attributes);
    }

    public boolean isUnicodeClass() {
        return TypeAttributes.UnicodeClass.isSet(attributes);
    }

    public void setUnicodeClass(boolean value) {
        attributes = TypeAttributes.UnicodeClass.set(value, attributes);
    }

    public boolean isAutoClass() {
        return TypeAttributes.AutoClass.isSet(attributes);
    }

    public void setAutoClass(boolean value) {
        attributes = TypeAttributes.AutoClass.set(value, attributes);
    }

    public boolean isBeforeFieldInit() {
        return TypeAttributes.BeforeFieldInit.isSet(attributes);
    }

    public void setBeforeFieldInit(boolean value) {
        attributes = TypeAttributes.BeforeFieldInit.set(value, attributes);
    }

    @Override
    public boolean isRuntimeSpecialName() {
        return TypeAttributes.RTSpecialName.isSet(attributes);
    }

    @Override
    public void setRuntimeSpecialName(boolean value) {
        attributes = TypeAttributes.RTSpecialName.set(value, attributes);
    }

    public boolean isHasSecurity() {
        return TypeAttributes.HasSecurity.isSet(attributes);
    }

    public void setHasSecurity(boolean value) {
        attributes = TypeAttributes.HasSecurity.set(value, attributes);
    }

    ////
    public boolean isEnum() {
        return baseType != null && baseType.isTypeOf("System", "Enum");
    }

    @Override
    public boolean isValueType() {
        //noinspection SimplifiableIfStatement
        if (baseType == null)
            return false;

        return baseType.isTypeOf("System", "Enum") || (baseType.isTypeOf("System", "ValueType") && !isTypeOf("System", "Enum"));
    }

    @Override
    public boolean isPrimitive() {
        return MetadataSystem.tryGetPrimitiveElementType(this).isPrimitive();
    }

    @Override
    public MetadataType getMetadataType() {
        ElementType elementType = MetadataSystem.tryGetPrimitiveElementType(this);
        return MetadataType.getByElementType(elementType);
    }

    @Override
    public boolean isDefinition() {
        return true;
    }

    @Override
    public TypeDefinition getDeclaringType() {
        return (TypeDefinition) super.getDeclaringType();
    }

    @Override
    public void setDeclaringType(TypeDefinition type) {
        super.setDeclaringType(type);
    }

    @Override
    public TypeDefinition resolve() {
        return this;
    }

    ////
    @Nullable
    public TypeDefinition getNestedType(String fullName) {
        if (!hasNestedTypes())
            return null;

        for (TypeDefinition definition : getNestedTypes()) {
            if (definition.getFullName().equals(fullName) || definition.getName().equals(fullName))
                return definition;
        }

        return null;
    }

    public TypeReference getEnumUnderlyingType() {
        for (FieldDefinition field : getFields()) {
            if (!field.isStatic())
                return field.getFieldType();
        }

        throw new IllegalStateException();
    }

    ////
    private void resolveLayout() {
        if (packingSize != Utils.NOT_RESOLVED_MARK || classSize != Utils.NOT_RESOLVED_MARK)
            return;

        if (!hasImage()) {
            packingSize = Utils.NO_DATA_MARK;
            classSize = Utils.NO_DATA_MARK;
            return;
        }

        Row2<Integer, Integer> row = getModule().read(this, MetadataReader::readTypeLayout);

        packingSize = row.getCol1();
        classSize = row.getCol2();
    }

    @Override
    public void dispose() {
        baseType = null;
        if (interfaces != null)
            interfaces.clear();
        if (nestedTypes != null)
            nestedTypes.clear();
        if (methods != null)
            methods.clear();
        if (fields != null)
            fields.clear();
        if (events != null)
            events.clear();
        if (properties != null)
            properties.clear();
        if (customAttributes != null)
            customAttributes.clear();
        if (securityDeclarations != null)
            securityDeclarations.clear();
    }
}
