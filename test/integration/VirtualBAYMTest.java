import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VirtualBAYMTest
{
	private static Stream<Arguments> getVoltageProvider()
	{
		// Assumes address is 1
		return Stream.of(
				Arguments.of( 120.0f, new byte[]{ 0x01, 0x04, 0x04, 0x42, (byte) 0xF0, 0x00, 0x00, (byte) 0xEF, (byte) 0xCF } ) );
	}

	@ParameterizedTest
	@MethodSource("getVoltageProvider")
	public void getVoltage( float voltage, byte[] expected )
	{
		byte address = 0x01;
		assertArrayEquals( (new VirtualBAYM( address )).getVoltage( 120.0f ), expected );
	}

	@Test
	public void getVoltage()
	{
		int address = 1;
		float voltage = 120.0f;
		ModbusDataAccess modbusDataAccess = new ModbusDataAccess( (new VirtualBAYM( address )).getVoltage( voltage ) );

		assertEquals( modbusDataAccess.getDataAsFloat(), voltage, 0.1 );
		assertEquals( modbusDataAccess.getAddress(), address );
		assertEquals( modbusDataAccess.getFunction(), ModbusDataAccess.ModbusFunction.ReadInputRegister );
	}
}

