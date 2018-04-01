package AcousticNetwork;

import java.io.FileInputStream;

// FileI need to be re constructured. As the need for it differs somehow.
public class FileI{
	public static final int BIN = 0;
	public static final int TEXT01 = 1;
	
	private int file_format_;
	private FileInputStream i_file_; 

	public FileI(String path, int file_format) throws Exception{
		file_format_ = file_format;
		i_file_ = new FileInputStream(path);
	}

	public void updateFile(String path){
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
		ArrayList<Byte> c = new ArrayList<Byte>();
		byte[] tmp = new byte[1];
		int r = this.read(tmp, 0, 1);
		while (r != -1){
			c.add(tmp[0]);
			r = this.read(tmp, 0, 1);
		}
		byte[] out = new byte[c.size()];
		for (int i=0; i<c.size(); i++){
			out[i] = get[i];
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