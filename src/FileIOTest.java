import AcousticNetwork.FileI;
import AcousticNetwork.FileO;

class FileIOTest{

	FileI i_file_;
	FileO o_file_;
	public FileIOTest() throws Exception{ 
		this("./I", FileI.bin,"./O", FileO.bin); 
	}
	public FileIOTest(
	  String i, int i_format, 
	  String o, int o_format) throws Exception{
		i_file_ = new FileI(i, i_format);
		o_file_ = new FileO(o, o_format);
	}

	public static void main(String[] args) throws Exception{
		FileIOTest file_io_test = new FileIOTest();
		if (args.length == 0){
			System.out.println("Please specify a function you want to test:\n" +
							" --save: To be filled \n" +
							" --read: To be filled \n" +
							" --both: To be filled");
		} else if (args[0].equals("--save")){

		} else if (args[0].equals("--read")){

		} else if (args[0].equals("--both")){

		} else {
			System.out.println("No such test.");
		}
	}
}