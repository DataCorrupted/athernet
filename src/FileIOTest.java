import AcousticNetwork.FileI;
import AcousticNetwork.FileO;

class FileIOTest{

	FileI i_file_;
	FileO o_file_;
	public FileIOTest() throws Exception{ 
		this("./I", FileI.text01,"./O", FileO.bin); 
	}
	public FileIOTest(
	  String i, int i_format, 
	  String o, int o_format) throws Exception{
		i_file_ = new FileI(i, i_format);
		o_file_ = new FileO(o, o_format);
	}

	private byte[] testRead(int byte_cnt) throws Exception{
		return this.i_file_.getBytes(byte_cnt);
	}

	private void testSave(byte[] f) throws Exception{
		this.o_file_.putBytes(f);
	}

	public static void main(String[] args) throws Exception{
		FileIOTest file_io_test = new FileIOTest();
		int byte_cnt = 1;
		if (args.length == 0){
			System.out.println("Please specify a function you want to test:\n" +
							" --save: Save a string \"Hello world.\" to O;\n" +
							" --read: Read content from I;\n" +
							" --both: Read content from I and output to O.");
		} else if (args[0].equals("--save")){
			byte[] f = {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 
						0x77, 0x6f, 0x72, 0x6c, 0x64, 0x2e};
			file_io_test.testSave(f);
		} else if (args[0].equals("--read")){
			byte[] f = file_io_test.testRead(byte_cnt);
			System.out.println(f);
		} else if (args[0].equals("--both")){
			byte[] f = file_io_test.testRead(byte_cnt);
			file_io_test.testSave(f);
		} else {
			System.out.println("No such test.");
		}
	}
}