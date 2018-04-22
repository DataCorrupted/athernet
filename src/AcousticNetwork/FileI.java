package AcousticNetwork;

import AcousticNetwork.FileIO;
import java.io.FileInputStream;

public class FileI extends FileIO {

	private FileInputStream i_file_; 

	public FileI(String path, int file_format) throws Exception{
		super(path, file_format);
		i_file_ = new FileInputStream(file_);
	}

	public int read(byte[] in, int offset, int len) throws Exception{
		int byte_cnt = len;
		int read_bytes = 
			(file_format_ == FileI.BIN)? len: len<<3;

		byte[] i_bytes = new byte[read_bytes];
		int r = i_file_.read(i_bytes, 0, read_bytes);
		if (r != -1){
			if (file_format_ == FileI.BIN){
				System.arraycopy(i_bytes, 0, in, offset, r);
			} else if (file_format_ == FileI.TEXT01){
				r  = r >>> 3;	
				text01ToBits(i_bytes, in, offset, r);
			}
		}
		return r;
	}

}