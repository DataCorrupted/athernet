/* Acknowledgment: 
 * Most code below comes from Stack Overflow.
 * (url: https://stackoverflow.com/questions/25284556/translate-crc8-from-c-to-java)
 * I used it in this project. I just changed the code style and claimed it in my package.
 * Such a coincident that initially my class called CRC8 too. :)
 */
package athernet.physical;
import java.util.zip.Checksum;

public class CRC8 implements Checksum{
	private final short init;
	private final short[] crcTable = new short[256];
	private short value;

	/**
	 * Construct a CRC8 specifying the polynomial and initial value.
	 * @param polynomial Polynomial, typically one of the POLYNOMIAL_* constants.
	 * @param init Initial value, typically either 0xff or zero.
	 */
	public CRC8(int polynomial, short init){
		this.value = this.init = init;
		// Table generation.
		for (int dividend = 0; dividend < 256; dividend++){
			int remainder = dividend ;//<< 8;
			for (int bit = 0; bit < 8; ++bit)
				if ((remainder & 0x01) != 0)
					remainder = (remainder >>> 1) ^ polynomial;
				else
					remainder >>>= 1;
			crcTable[dividend] = (short)remainder;
		}
	}

	@Override
	public void update(byte[] buffer, int offset, int len){
		int data;
		for (int i = 0; i < len; i++){
			data = buffer[offset+i] ^ value;
			value = (short)(crcTable[data & 0xff] ^ (value << 8));
		}
	}

	/**
	 * Updates the current checksum with the specified array of bytes.
	 * Equivalent to calling update(buffer, 0, buffer.length).
	 * @param buffer the byte array to update the checksum with
	 */
	public void update(byte[] buffer){
		update(buffer, 0, buffer.length);
	}

	@Override
	public void update(int b){
		update(new byte[]{(byte)b}, 0, 1);
	}

	@Override
	public long getValue(){
		return value & 0xff;
	}

	@Override
	public void reset(){
		value = init;
	}

	public static void  main(String[] args){
		final int CRC_POLYNOM = 0x9C; 		// 1001 1100
		final byte CRC_INITIAL = (byte)0xFF;

		final byte[] data = {1, 56, -23, 3, 0, 19, 0, 0, 2, 0, 3, 13, 8, -34, 7, 9, 42, 18, 26, -5, 54, 11, -94, -46, -128, 4, 48, 52, 0, 0, 0, 0, 0, 0, 0, 0, 4, 1, 1, -32, -80, 0, 98, -5, 71, 0, 64, 0, 0, 0, 0, -116, 1, 104, 0};
		CRC8 crc8 = new CRC8(CRC_POLYNOM, CRC_INITIAL);
		crc8.update(data, 0, data.length-1);
		System.out.printf("%d\n", crc8.getValue());
		data[data.length-1] = (byte) crc8.getValue();

		crc8.reset();
		crc8.update(data, 0, data.length-1);
		System.out.printf("%d\n", crc8.getValue());
	}
}