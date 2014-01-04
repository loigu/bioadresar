package cz.hnutiduha.bioadresar.data;

public class ActivityWithComment extends StringifiedFromDb{
	
	public ActivityWithComment(ActivityWithComment origin)
	{
		super(origin.id, origin.comment, origin.db);
	}
	
	ActivityWithComment(long id, String comment, HnutiduhaFarmDb db) {
		super(id, comment, db);
	}

	public ActivityWithComment(long id, HnutiduhaFarmDb db) {
		super(id, db);
	}

	protected String getName(HnutiduhaFarmDb db) {
		return db.getActivityName(id);
	}
}