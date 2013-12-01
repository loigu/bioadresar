package cz.hnutiduha.bioadresar.data;

public class ProductWithComment extends StringifiedFromDb{
	
	ProductWithComment(long id, String comment, HnutiduhaFarmDb db) {
		super(id, comment, db);
	}
	
	public ProductWithComment(long id, HnutiduhaFarmDb db) {
		super(id,  db);
	}

	protected String getName(HnutiduhaFarmDb db) {
		return db.getProductName(id);
	}
	
}