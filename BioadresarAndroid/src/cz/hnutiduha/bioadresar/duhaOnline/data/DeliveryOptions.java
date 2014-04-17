package cz.hnutiduha.bioadresar.duhaOnline.data;

public class DeliveryOptions {
	public String [] placesWithTime = null;
	public boolean customDistribution = false;
	
	public DeliveryOptions(DeliveryOptions origin) {
		if (origin == null)
		{
			return;
		}
		
		customDistribution = origin.customDistribution;
		if (origin.placesWithTime != null)
		{
			placesWithTime = new String[origin.placesWithTime.length];
			System.arraycopy(origin.placesWithTime, 0, placesWithTime, 0, origin.placesWithTime.length);
		}
	}

	public DeliveryOptions() {
	}
}
