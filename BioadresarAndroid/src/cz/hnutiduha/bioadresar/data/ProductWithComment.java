package cz.hnutiduha.bioadresar.data;

public class ProductWithComment extends StringifiedFromDb{
	public long id;
	
	public ProductWithComment(long id, String comment) {
		this.id = id;
		this.comment = comment;
	}
	protected String getName(DatabaseHelper db) {
		return db.getProductName(id);
	}
	
}