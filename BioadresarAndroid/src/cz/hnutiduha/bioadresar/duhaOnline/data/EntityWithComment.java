package cz.hnutiduha.bioadresar.duhaOnline.data;

import java.util.Comparator;

public class EntityWithComment {
	protected String name;
	protected String comment;
	protected boolean mainEntity; ///< duha "především"

	public EntityWithComment(String name, String comment, boolean mainEntity) {
		this.name = name;
		this.comment = comment;
		this.mainEntity = mainEntity;
	}
	
	public EntityWithComment(EntityWithComment origin)
	{
		this.name = origin.name;
		this.comment = origin.comment;
		this.mainEntity = origin.mainEntity;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String toString() {
		if (comment != null && comment.length() != 0)
			return name + " (" + comment + ")";
		
		return name;
	}
	
	public int compareTo(EntityWithComment other)
	{
		// we are lazy to do <> on booleans
		if (this.mainEntity != other.mainEntity)
			return -1;
		
		return this.toString().compareTo(other.toString());
	}
	
	static Comparator<EntityWithComment> stringComparator()
	{
		return new Comparator<EntityWithComment> ()
				{

					@Override
					public int compare(EntityWithComment lhs,
							EntityWithComment rhs) {
						return lhs.toString().compareTo(rhs.toString());
					}
			
				};
	}

}
