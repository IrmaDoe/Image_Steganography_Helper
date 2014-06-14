package edu.hit.ict.ish;

public final class JpegStegInfo {
	public int stegStat;//0:NO, 1:MESSAGE, 2:FILE
	public long capacity;//NO, MESSAGE, FILE
	public long secretSize;//MESSAGE, FILE
	public float usageRate;//MESSAGE, FILE
	
	JpegStegInfo() {
		stegStat = 0;
		capacity = 0;
		secretSize = 0;
		usageRate = 0;
	}
}
