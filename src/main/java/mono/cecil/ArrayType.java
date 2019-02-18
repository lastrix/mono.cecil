package mono.cecil;

import java.util.ArrayList;
import java.util.List;

public class ArrayType extends TypeSpecification
{
	private final List<ArrayDimension> dimensions;

	public ArrayType( TypeReference type )
	{
		this( type, 1 );
	}

	private ArrayType( TypeReference type, int rank )
	{
		super( type );
		checkType( type );
		setEtype( ElementType.Array );
		dimensions = new ArrayList<>();
		while( rank > 0 )
		{
			dimensions.add( new ArrayDimension() );
			rank--;
		}
	}

	public int getRank()
	{
		return dimensions == null ? 1 : dimensions.size();
	}

	public boolean isVector()
	{
		if( dimensions == null )
			return true;

		if( dimensions.size() > 1 )
			return false;

		ArrayDimension dimension = dimensions.get( 0 );
		return !dimension.isSized();
	}

	@Override
	public boolean isValueType()
	{
		return false;
	}

	@Override
	public void setValueType( boolean valueType )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName()
	{
		return super.getName() + getSuffix();
	}

	private String getSuffix()
	{
		if( isVector() ) return "[]";

		StringBuilder sb = new StringBuilder();
		sb.append( '[' );
		boolean first = true;
		for( ArrayDimension dimension : dimensions )
		{
			if( first )
				first = false;
			else sb.append( ',' );
			sb.append( dimension );
		}
		sb.append( ']' );
		return sb.toString();
	}

	@Override
	public boolean isArray()
	{
		return true;
	}

	public void clearDimensions()
	{
		dimensions.clear();
	}

	public void addDimension( ArrayDimension dimension )
	{
		dimensions.add( dimension );
	}

}
