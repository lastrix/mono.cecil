package mono.cecil;

import mono.cecil.metadata.MetadataToken;

public enum CodedIndex
{
	TypeDefOrRef,
	HasConstant,
	HasCustomAttribute,
	HasFieldMarshal,
	HasDeclSecurity,
	MemberRefParent,
	HasSemantics,
	MethodDefOrRef,
	MemberForwarded,
	Implementation,
	CustomAttributeType,
	ResolutionScope,
	TypeOrMethodDef;

	@SuppressWarnings( {"SwitchStatementDensity", "NestedSwitchStatement", "MagicNumber"} )
	public MetadataToken getMetadataToken( int data )
	{
		int rid;
		TokenType token_type;
		switch( this )
		{
			case TypeDefOrRef:
				rid = data >> 2;
				switch( data & 3 )
				{
					case 0:
						token_type = TokenType.TypeDef;
						break;
					case 1:
						token_type = TokenType.TypeRef;
						break;
					case 2:
						token_type = TokenType.TypeSpec;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case HasConstant:
				rid = data >> 2;
				switch( data & 3 )
				{
					case 0:
						token_type = TokenType.Field;
						break;
					case 1:
						token_type = TokenType.Param;
						break;
					case 2:
						token_type = TokenType.Property;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case HasCustomAttribute:
				rid = data >> 5;
				switch( data & 31 )
				{
					case 0:
						token_type = TokenType.Method;
						break;
					case 1:
						token_type = TokenType.Field;
						break;
					case 2:
						token_type = TokenType.TypeRef;
						break;
					case 3:
						token_type = TokenType.TypeDef;
						break;
					case 4:
						token_type = TokenType.Param;
						break;
					case 5:
						token_type = TokenType.InterfaceImpl;
						break;
					case 6:
						token_type = TokenType.MemberRef;
						break;
					case 7:
						token_type = TokenType.Module;
						break;
					case 8:
						token_type = TokenType.Permission;
						break;
					case 9:
						token_type = TokenType.Property;
						break;
					case 10:
						token_type = TokenType.Event;
						break;
					case 11:
						token_type = TokenType.Signature;
						break;
					case 12:
						token_type = TokenType.ModuleRef;
						break;
					case 13:
						token_type = TokenType.TypeSpec;
						break;
					case 14:
						token_type = TokenType.Assembly;
						break;
					case 15:
						token_type = TokenType.AssemblyRef;
						break;
					case 16:
						token_type = TokenType.File;
						break;
					case 17:
						token_type = TokenType.ExportedType;
						break;
					case 18:
						token_type = TokenType.ManifestResource;
						break;
					case 19:
						token_type = TokenType.GenericParam;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case HasFieldMarshal:
				rid = data >> 1;
				switch( data & 1 )
				{
					case 0:
						token_type = TokenType.Field;
						break;
					case 1:
						token_type = TokenType.Param;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case HasDeclSecurity:
				rid = data >> 2;
				switch( data & 3 )
				{
					case 0:
						token_type = TokenType.TypeDef;
						break;
					case 1:
						token_type = TokenType.Method;
						break;
					case 2:
						token_type = TokenType.Assembly;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case MemberRefParent:
				rid = data >> 3;
				switch( data & 7 )
				{
					case 0:
						token_type = TokenType.TypeDef;
						break;
					case 1:
						token_type = TokenType.TypeRef;
						break;
					case 2:
						token_type = TokenType.ModuleRef;
						break;
					case 3:
						token_type = TokenType.Method;
						break;
					case 4:
						token_type = TokenType.TypeSpec;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case HasSemantics:
				rid = data >> 1;
				switch( data & 1 )
				{
					case 0:
						token_type = TokenType.Event;
						break;
					case 1:
						token_type = TokenType.Property;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case MethodDefOrRef:
				rid = data >> 1;
				switch( data & 1 )
				{
					case 0:
						token_type = TokenType.Method;
						break;
					case 1:
						token_type = TokenType.MemberRef;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case MemberForwarded:
				rid = data >> 1;
				switch( data & 1 )
				{
					case 0:
						token_type = TokenType.Field;
						break;
					case 1:
						token_type = TokenType.Method;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case Implementation:
				rid = data >> 2;
				switch( data & 3 )
				{
					case 0:
						token_type = TokenType.File;
						break;
					case 1:
						token_type = TokenType.AssemblyRef;
						break;
					case 2:
						token_type = TokenType.ExportedType;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case CustomAttributeType:
				rid = data >> 3;
				switch( data & 7 )
				{
					case 2:
						token_type = TokenType.Method;
						break;
					case 3:
						token_type = TokenType.MemberRef;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case ResolutionScope:
				rid = data >> 2;
				switch( data & 3 )
				{
					case 0:
						token_type = TokenType.Module;
						break;
					case 1:
						token_type = TokenType.ModuleRef;
						break;
					case 2:
						token_type = TokenType.AssemblyRef;
						break;
					case 3:
						token_type = TokenType.TypeRef;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			case TypeOrMethodDef:
				rid = data >> 1;
				switch( data & 1 )
				{
					case 0:
						token_type = TokenType.TypeDef;
						break;
					case 1:
						token_type = TokenType.Method;
						break;
					default:
						return MetadataToken.ZERO;
				}
				break;

			default:
				return MetadataToken.ZERO;
		}

		return new MetadataToken( token_type, rid );
	}


}
