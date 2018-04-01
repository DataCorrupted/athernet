package AcousticNetwork;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.File;

public class FileI{
	public static final int BIN = 0;
	public static final int TEXT01 = 1;
	
	private File file_;
	private String path_;
	private int file_format_;
	private FileInputStream i_file_; 

	public FileI(String path, int file_format) throws Exception{
		updateFile(path);
		file_format_ = file_format;
	}

	public void updateFile(String path) throws Exception{
		path_ = path;
		file_ = new File(path);
		i_file_ = new FileInputStream(path);
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

	public byte[] readAllData() throws Exception{
		byte[] out = new byte[(int)file_.length()];
		i_file_.read(out);
			if (file_format_ == FileI.BIN){
				System.arraycopy(i_bytes, 0, in, offset, r);
			} else if (file_format_ == FileI.TEXT01){
				r  = r >>> 3;	
				text01ToBits(i_bytes, in, offset, r);
			}
		if (file_format_ = FileI.TEXT01){
			text01ToBits(out, )
		}
		return out;
	}

	private void text01ToBits(byte[] src, byte[] dst, int offset, int len){
		for (int i=0; i<len; i++){
			for (int j=0; j<8; j++){
				dst[i + offset] = (byte) (dst[i + offset] << 1);
				dst[i + offset] += (src[(i<<3)+j] == (byte)'1')? 1: 0;
			}
		}
	}
}