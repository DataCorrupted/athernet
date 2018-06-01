package athernet.nat;

import java.io.BufferedReader;
import java.io.FileReader;

public class TestNat {
    private BufferedReader reader_;

    public static void main(String[] args) throws Exception{
        FileReader file_reader = new FileReader("/home/ernest/athernet/input/input.txt");
        BufferedReader reader = new BufferedReader(file_reader);
        for(int i  = 0; i < 2; i++){
            String out = reader.readLine();
            System.out.println(out);
        }
    }
}
