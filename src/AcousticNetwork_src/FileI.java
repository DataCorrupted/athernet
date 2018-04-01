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
				byte[] tmp = text01ToBits(i_bytes);
				System.arraycopy(tmp, 0, in, offset, r);
			}
		}
		return r;
	}

	public byte[] readAllData() throws Exception{
		byte[] out = new byte[(int)file_.length()];
		i_file_.read(out);
		if (file_format_ == FileI.BIN){
			return out;
		} else {
			return text01ToBits(out);
		}
	}

	private byte[] text01ToBits(byte[] src){
		int len = src.length >>> 3;
		byte[] dst = new byte[len];
		for (int i=0; i<len; i++){
			for (int j=0; j<8; j++){
				dst[i] = (byte) (dst[i] << 1);
				dst[i] += (src[(i<<3)+j] == (byte)'1')? 1: 0;
			}
		}
		return dst;
	}

	public static void main(String[] args) throws Exception{
		FileI i_file = new FileI("./I", FileI.TEXT01);
		byte[] data = i_file.readAllData();
		for (int i=0; i<data.length; i++){
			System.out.print((char) data[i]);
		}
		System.out.println();
	}
}