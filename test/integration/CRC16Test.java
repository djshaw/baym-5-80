import static org.junit.Assert.assertArrayEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CRC16Test
{
	private static Stream<Arguments> crc16TestProvider()
	{
		return Stream.of(
				Arguments.of( new byte[]{ 0x01, 0x04, 0x00, 0x00, 0x00, 0x02 },       new byte[]{ 0x71, (byte) 0xCB } ),
				Arguments.of( new byte[]{ 0x01, 0x04, 0x04, 0x43, 0x6B, 0x58, 0x0E }, new byte[]{ 0x25, (byte) 0xD8 } ) );
	}

	@ParameterizedTest
	@MethodSource("crc16TestProvider")
	public void crc16( byte[] input, byte[] expected )
	{
		CRC16 crc = new CRC16();
		crc.update( input );
		assertArrayEquals( crc.getValue(), expected );
	}
}

