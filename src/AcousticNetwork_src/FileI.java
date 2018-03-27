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

	public byte[] getBytes(int byte_cnt) throws Exception{
		int read_bytes = 
			(file_format_ == FileI.bin)? byte_cnt: byte_cnt<<3;
		byte[] i_bytes = new byte[read_bytes];
		i_file_.read(i_bytes, 0, read_bytes);

		byte[] in = new byte[byte_cnt];
		if (file_format_ == FileI.bin){
			in = i_bytes;
		} else if (file_format_ == FileI.text01){
			in = text01ToBits(i_bytes);
		}
		return in;
	}

	private byte[] text01ToBits(byte[] text){
		// Bugy
		byte[] in = new byte[text.length >> 3];
		for (int i=0; i<text.length; i++){
			for (int j=0; j<1; j++){
				if (text[i<<3+j] == (byte)'1'){
					in[i] ++;
				}
				in[i] = (byte) (in[i] << 1);
			}
		}
		return in;

	}
}