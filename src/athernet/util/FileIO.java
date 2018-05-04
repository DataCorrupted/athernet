package athernet.util;

import java.io.File;
import athernet.util.FileI;
import athernet.util.FileO;

public class FileIO{
	public static final int BIN = 0;
	public static final int TEXT01 = 1;
	
	protected int file_format_;
	protected File file_; 

	protected FileIO(String path, int file_format) throws Exception{
		file_format_ = file_format;
		file_ = new File(path);
	}
	protected byte[] bitsToText01(byte[] bytes){
		byte[] out = new byte[bytes.length << 3];
		for (int i=0; i<bytes.length; i++){
			for (int j=0; j<8; j++){
				out[(i<<3)+7-j] = (byte) (((bytes[i] & 1) == 1)? '1':'0');
				bytes[i] = (byte) (bytes[i] >>> 1);
			}
		}
		return out;
	}
	protected void text01ToBits(byte[] src, byte[] dst, int offset, int len){
		for (int i=0; i<len; i++){
			for (int j=0; j<8; j++){
				dst[i + offset] = (byte) (dst[i + offset] << 1);
				dst[i + offset] += (src[(i<<3)+j] == (byte)'1')? 1: 0;
			}
		}
	}
	public int getSize(){
		return (int) file_.length();
	}

	public static void main(String[] args) throws Exception{
		FileI i_file = new FileI("./I", BIN);
		FileO o_file = new FileO("./O", TEXT01);
		
		System.out.println(i_file.getSize() + "bytes in total.");
		
		int r=0;
		byte[] f = new byte[1000];
		while (r!= -1){
			r = i_file.read(f, 0, f.length);
			o_file.write(f, 0, r);
		}
	}
}