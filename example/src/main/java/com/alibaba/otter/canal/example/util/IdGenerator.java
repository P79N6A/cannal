package com.alibaba.otter.canal.example.util;

public class IdGenerator {
	/**
	 * ����ϵͳʱ������к�����ID
	 * @return
	 */
	public static String getId() {
		return new StringBuffer(16).append(format(getHiTime())).append(
				format(getLoTime())).append(format(getCount())).toString();
	}
	/**
	 * ����ϵͳʱ�䡢���кźͲ��������ĵ�ID
	 * @param dictCode
	 * @return
	 */
	public static String getDocId(String param) {
		return new StringBuffer(16).append(param).append(format(getHiTime())).append(
				format(getLoTime())).append(format(getCount())).toString();
	}

	private static short getCount() {
		synchronized(IdGenerator.class) {
			if (counter<0) counter=0;
			return counter++;
		}
	}

	private static short getHiTime() {
		return (short) ( System.currentTimeMillis() >>> 32 );
	}
	private static int getLoTime() {
		return (int) System.currentTimeMillis();
	}
	private static String format(short shortval) {
		String formatted = Integer.toHexString(shortval);
		StringBuffer buf = new StringBuffer("0000");
		buf.replace( 4-formatted.length(), 4, formatted );
		return buf.toString();
	}
	private static String format(int intval) {
		String formatted = Integer.toHexString(intval);
		StringBuffer buf = new StringBuffer("00000000");
		buf.replace( 8-formatted.length(), 8, formatted );
		return buf.toString();
	}
	private static short counter = (short) 0;
	public static void main( String[] args ) throws Exception {
	

		
			String id = IdGenerator.getId();
			System.out.println(id);
			

	}
}
