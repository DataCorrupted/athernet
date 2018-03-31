# acoustic_network
A Network using sound to transmit information.

# data packet definition
crc8(1 byte) + packet_id(1 byte) + data (n bytes)

# special packet_id
- 0-252 => data packet
- 253 => empty dummy packet
- 254 => NAK Report
- 255 => non-exist (useful in requirement of zero-padding)

## Note:
In Java, byte is [-128,127], so the above 255 should be -1. Same as the rest.

# modulated soundwave definition
