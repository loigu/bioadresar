package cz.hnutiduha.bioadresar.data;

public class ActivityWithComment extends StringifiedFromDb{
	public long id;
	
	public ActivityWithComment(long id, String comment) {
		this.id = id;
		this.comment = comment;
	}
	
	protected String getName(DatabaseHelper db) {
		return db.getActivityName(id);
	}
}