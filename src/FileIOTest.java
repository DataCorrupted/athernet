import AcousticNetwork.FileI;
import AcousticNetwork.FileO;

class FileIOTest{

	FileI i_file_;
	FileO o_file_;
	public FileIOTest() throws Exception{ 
		this("./I", FileI.TEXT01,"./O", FileO.TEXT01); 
	}
	public FileIOTest(
	  String i, int i_format, 
	  String o, int o_format) throws Exception{
		i_file_ = new FileI(i, i_format);
		o_file_ = new FileO(o, o_format);
	}

	private int testRead(byte[] f) throws Exception{
		int r = this.i_file_.getBytes(f);
		System.out.print(r + " bytes read: \n\"\"\"\n");
		for (int i=0; i<r; i++){
			System.out.print((char) f[i]);
		}
		System.out.print("\n\"\"\"\n");
		return r;
	}

	private void testSave(byte[] f, int byte_cnt) throws Exception{
		this.o_file_.putBytes(f, byte_cnt);
	}

	public static void main(String[] args) throws Exception{
		FileIOTest file_io_test = new FileIOTest("./I", FileI.TEXT01,"./O", FileO.BIN);
		int byte_cnt = 1000;
		byte[] f = new byte[byte_cnt];

		if (args.length == 0){
			System.out.println("Please specify a function you want to test:\n" +
							" --save: Save a string \"Hello world\" to O;\n" +
							" --read: Read content from I;\n" +
							" --both: Read content from I and output to O.");
		} else if (args[0].equals("--save")){
			byte[] hello = {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20,  
							0x77, 0x6f, 0x72, 0x6c, 0x64, 0x2e};
			file_io_test.testSave(hello, 12);
		} else if (args[0].equals("--read")){
			file_io_test.testRead(f);
			file_io_test.testRead(f);
		} else if (args[0].equals("--both")){
			int r;

			r = file_io_test.testRead(f);
			file_io_test.testSave(f, r);
			// Don't worry about the extra 0s here.
			// Higher class will guarantee that no extra 0
			// will be provided.
			r = file_io_test.testRead(f);
			file_io_test.testSave(f, r);

		} else {
			System.out.println("No such test.");
		}
	}
}