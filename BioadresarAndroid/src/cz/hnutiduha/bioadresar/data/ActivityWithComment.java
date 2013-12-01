package cz.hnutiduha.bioadresar.data;

public class ActivityWithComment extends StringifiedFromDb{
	
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