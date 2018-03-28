package AcousticNetwork;

import java.io.FileOutputStream;

public class FileO{
	public static final int bin = 0;
	public static final int text01 = 1;

	int file_format_;
	FileOutputStream o_file_; 

	public FileO() throws Exception{
		this("./O", 1);
	}
	public FileO(String path, int file_format) throws Exception{
		file_format_ = file_format;
		o_file_ = new FileOutputStream(path);
	}
	public void putBytes(byte[] f) throws Exception{
		if (file_format_ == FileO.text01){
			f = bitsToText01(f);
		}
		o_file_.write(f);
	}
	private byte[] bitsToText01(byte[] bytes){
		byte[] out = new byte[bytes.length << 3];
		for (int i=0; i<bytes.length; i++){
			for (int j=0; j<8; j++){
				out[(i<<3)+7-j] = (byte) (((bytes[i] & 1) == 1)? '1':'0');
				bytes[i] = (byte) (bytes[i] >>> 1);
			}
		}
		return out;
	}
}