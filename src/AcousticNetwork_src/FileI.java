package AcousticNetwork;

import java.io.FileInputStream;

public class FileI{
	public static final int bin = 0;
	public static final int text01 = 1;
	int file_format_;
	FileInputStream i_file_; 


	public FileI(String path, int file_format) throws Exception{
		file_format_ = file_format;
		i_file_ = new FileInputStream(path);
	}

	public int getBytes(byte[] in) throws Exception{
		int byte_cnt = in.length;
		int read_bytes = 
			(file_format_ == FileI.bin)? byte_cnt: byte_cnt<<3;
		byte[] i_bytes = new byte[read_bytes];
		int r = i_file_.read(i_bytes, 0, read_bytes);

		if (file_format_ == FileI.bin){
			in = i_bytes;
		} else if (file_format_ == FileI.text01){
			text01ToBits(in, i_bytes);
			r  = r >>> 3;
		}
		return r;
	}

	private void text01ToBits(byte[] dst, byte[] src){
		// Bugy
		for (int i=0; i<dst.length; i++){
			for (int j=0; j<8; j++){
				dst[i] = (byte) (dst[i] << 1);
				dst[i] += (src[(i<<3)+j] == (byte)'1')? 1: 0;
			}
		}
	}
}