package AcousticNetwork;

import java.io.FileOutputStream;

public class FileO{
	public static final int BIN = 0;
	public static final int TEXT01 = 1;

	private int file_format_;
	private FileOutputStream o_file_; 

	public FileO() throws Exception{
		this("./O", 1);
	}
	public FileO(String path, int file_format) throws Exception{
		file_format_ = file_format;
		o_file_ = new FileOutputStream(path);
	}
	public void putBytes(byte[] f, int byte_cnt) throws Exception{
		if (byte_cnt <= 0) { return; }

		if (file_format_ == FileO.TEXT01){
			byte_cnt = byte_cnt << 3;
			f = bitsToText01(f);
		}
		o_file_.write(f, 0, byte_cnt);
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