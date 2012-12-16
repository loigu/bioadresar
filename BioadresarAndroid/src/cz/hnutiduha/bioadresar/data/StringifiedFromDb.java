package cz.hnutiduha.bioadresar.data;

public abstract class StringifiedFromDb {
	String comment;
	protected abstract String getName(DatabaseHelper db);
	
	public String toString(DatabaseHelper db) {
		String ret = getName(db);
		if (comment != null && comment.length() != 0)
			ret += " (" + comment + ")";
		return ret;
	}
}
