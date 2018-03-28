package AcousticNetwork;

import java.io.FileInputStream;

public class FileI{
	public static final int BIN = 0;
	public static final int TEXT01 = 1;
	
	private int file_format_;
	private FileInputStream i_file_; 

	public FileI(String path, int file_format) throws Exception{
		file_format_ = file_format;
		i_file_ = new FileInputStream(path);
	}

	public int getBytes(byte[] in) throws Exception{
		int byte_cnt = in.length;
		int read_bytes = 
			(file_format_ == FileI.BIN)? byte_cnt: byte_cnt<<3;
		byte[] i_bytes = new byte[read_bytes];
		int r = i_file_.read(i_bytes, 0, read_bytes);
		if (r != -1){
			if (file_format_ == FileI.BIN){
				System.arraycopy(i_bytes, 0, in, 0, r);
			} else if (file_format_ == FileI.TEXT01){
				r  = r >>> 3;	
				text01ToBits(i_bytes, in, r);
			}
		}
		return r;
	}

	private void text01ToBits(byte[] src, byte[] dst, int length){
		for (int i=0; i<length; i++){
			for (int j=0; j<8; j++){
				dst[i] = (byte) (dst[i] << 1);
				dst[i] += (src[(i<<3)+j] == (byte)'1')? 1: 0;
			}
		}
	}
}