public class DDSHelper implements DDSFile {
	private static final byte[] magic = new byte[] {0x44, 0x44, 0x53, 0x20};
	private byte[] data;
	private int width;
	private int height;
	
	public static DDSFile parse(byte[] data) {
		DDSHelper helper = new DDSHelper();
		helper.data = new byte[data.length-128];
		helper.height = (data[12]&0xFF) | ((data[13]&0xFF)<<8) | ((data[14]&0xFF)<<16) | ((data[15]&0xFF)<<24);
		helper.width = (data[16]&0xFF) | ((data[17]&0xFF)<<8) | ((data[18]&0xFF)<<16) | ((data[19]&0xFF)<<24);
		System.arraycopy(data, 128, helper.data, 0, helper.data.length);
		return helper;
	}


	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public int getWidth() {
		return width;
	}


	@Override
	public int getHeight() {
		return height;
	}
}
