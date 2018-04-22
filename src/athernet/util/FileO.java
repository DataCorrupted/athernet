package athernet.util;

import java.io.FileOutputStream;

public class FileO extends FileIO{
	private FileOutputStream o_file_; 

	public FileO() throws Exception{
		this("./O", TEXT01);
	}
	public FileO(String path, int file_format) throws Exception{
		super(path, file_format);
		o_file_ = new FileOutputStream(file_);
	}

	public void write(byte[] f, int offset, int len) throws Exception{
		if (len <= 0) { return; }

		if (file_format_ == FileO.TEXT01){
			len = len << 3;
			offset = offset << 3;
			f = bitsToText01(f);
		}
		o_file_.write(f, offset, len);
	}
}