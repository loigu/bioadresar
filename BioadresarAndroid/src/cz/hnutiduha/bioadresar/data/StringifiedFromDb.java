package cz.hnutiduha.bioadresar.data;

import java.util.Comparator;

public abstract class StringifiedFromDb implements Comparable<StringifiedFromDb>{
	String comment;
	public long id;
	HnutiduhaFarmDb db;
	
	StringifiedFromDb(long id, HnutiduhaFarmDb db)
	{
		this.id = id;
		this.db = db;
	}
	
	StringifiedFromDb(long id, String comment, HnutiduhaFarmDb db)
	{
		this.id = id;
		this.comment = comment;
		this.db = db;
	}
	
	protected abstract String getName(HnutiduhaFarmDb db);
	
	public String toString() {
		String ret = getName(db);
		if (comment != null && comment.length() != 0)
			ret += " (" + comment + ")";
		return ret;
	}
	
	public int compareTo(StringifiedFromDb other)
	{
		if (id != other.id)
			return (int)(id - other.id);
		else if (this.comment == null && other.comment != null)
			return -1;
		else 
			return this.comment.compareTo(other.comment);
	}
	
	static Comparator<StringifiedFromDb> stringComparator(final HnutiduhaFarmDb db)
	{
		return new Comparator<StringifiedFromDb> ()
				{

					@Override
					public int compare(StringifiedFromDb lhs,
							StringifiedFromDb rhs) {
						return lhs.toString().compareTo(rhs.toString());
					}
			
				};
	}
}


