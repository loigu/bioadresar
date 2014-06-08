package cz.hnutiduha.bioadresar.duhaOnline.data;

import java.util.Comparator;

public class EntityWithComment {
	public final static int INVALID_ID = -1;
	protected int id = INVALID_ID;
	protected String name;
	protected String comment;
	protected boolean mainEntity; ///< duha "především"

	public EntityWithComment(int id, String name, String comment, boolean mainEntity) {
		this.id = id;
		this.name = name;
		this.comment = comment;
		this.mainEntity = mainEntity;
	}
	public EntityWithComment(String name, String comment, boolean mainEntity) {
		this(-1, name, comment, mainEntity);
	}
	
	public EntityWithComment(EntityWithComment origin)
	{
		this.id = origin.id;
		this.name = origin.name;
		this.comment = origin.comment;
		this.mainEntity = origin.mainEntity;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String toString() {
		if (comment != null && comment.length() != 0)
			return name + " (" + comment + ")";
		
		return name;
	}
	
	// NOTE: this is complete junk
	public int compareTo(EntityWithComment other)
	{
		// TODO: what about ids?
		
		int res = name.compareTo(other.name);
		if (res != 0) { return res; }
		
		if (comment != null && other.comment == null)
		{
			return -1;
		}
		else if (comment == null && other.comment != null)
		{
			return 1;
		}
		
		res = comment.compareTo(other.comment);
		if (res != 0) { return res; }
		
		if (this.mainEntity != other.mainEntity) return -1;
		
		return 0;
	}
	
	static Comparator<EntityWithComment> stringComparator()
	{
		return new Comparator<EntityWithComment> ()
				{

					@Override
					public int compare(EntityWithComment lhs,
							EntityWithComment rhs) {
						return lhs.compareTo(rhs);
					}
			
				};
	}
	
	static Comparator<EntityWithComment> idComparator()
	{
		return new Comparator<EntityWithComment> ()
				{
					@Override
					public int compare(EntityWithComment lhs,
							EntityWithComment rhs) {
						return lhs.id - rhs.id;
					}
				};
	}

}
