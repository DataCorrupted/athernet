class Demodulation{
	private byte[] packet;
	private is_data = false;

	/*
	Return value:
		True: when a complete datapacket is ready.
		False: Otherwise
	*/
	public bool demodulation(double sample){
		// given a recved signal. Demodulate it. 
		// Whether current signals are data or just nothing important.
		/*
		if (is_data) {
			if can be demodulated to 0 or 1 {
				put 0/1 in packet
				if (packet is full){
					is_data = false
				}
			} else {
				wait for more sample until it can be demodulated.
				( Should have same samples for each bit)
			}
		} else {
			match for head.
			If head matched, {
				is_data = true;
			}
		}
		return is_packet_full;
	}	*/

	/*
	Return value:
		Return a empry byte[] when no packet available.
	*/	
	public byte[] getPacket(){
		return packet
	}
}
