import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ModbusDataAccessTest
{
	private static Stream< Arguments > shortToInputRegisterEnumProvider()
	{
		return Stream.of(
				Arguments.of( (short) 0, ModbusDataAccess.BAYMInputRegisterAddress.VOLTAGE_REGISTER_ADDRESS ) );
	}

	@ParameterizedTest
	@MethodSource("shortToInputRegisterEnumProvider")
	public void shortToInputRegisterEnum( short b, ModbusDataAccess.BAYMInputRegisterAddress expected )
	{
		assertEquals( ModbusDataAccess.shortToInputRegisterEnum( b ), expected );
	}

	private static Stream< Arguments > getFunctionProvider()
	{
		return Stream.of(
				Arguments.of( new byte[]{ 0x01, 0x03 }, ModbusDataAccess.ModbusFunction.ReadHoldingRegister ),
				Arguments.of( new byte[]{ 0x01, 0x04 }, ModbusDataAccess.ModbusFunction.ReadInputRegister ) );
	}

	@ParameterizedTest
	@MethodSource("getFunctionProvider")
	public void getFunction( byte[] input, ModbusDataAccess.ModbusFunction expected )
	{
		assertTrue( (new ModbusDataAccess( input )).getFunction() == expected );
	}

	private static Stream< Arguments > getAddressProvider()
	{
		return Stream.of( 
				Arguments.of( new byte[]{ 0x03 }, (byte) 3 ),
				Arguments.of( new byte[]{ 0x04 }, (byte) 4 ) );
	}

	@ParameterizedTest
	@MethodSource("getAddressProvider")
	public void getAddress( byte[] input, byte expected )
	{
		assertEquals( (new ModbusDataAccess( input )).getAddress(), expected );
	}

	private static Stream< Arguments > getDataProvider()
	{
		return Stream.of(
				Arguments.of( new byte[]{ 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 }, new byte[]{ 0x03, 0x04 } ) );
	}

	@ParameterizedTest
	@MethodSource("getDataProvider")
	public void getData( byte[] input, byte[] expected )
	{
		assertArrayEquals( (new ModbusDataAccess( input )).getData(), expected );
	}

	private static Stream< Arguments > getDataAsFloatProvider()
	{
		return Stream.of(
				Arguments.of( new byte[]{ 0x01, 0x02, 0x04, 0x43, 0x6B, 0x58, 0x0E, 0x00, 0x00 }, (float) 235.343963623046875 ) );
	}

	@ParameterizedTest
	@MethodSource("getDataAsFloatProvider")
	public void getDataAsFloat( byte[] input, float expected )
	{
		assertEquals( (new ModbusDataAccess( input )).getDataAsFloat(), expected, 0.1 );
	}

	private static Stream< Arguments > getDataAsRegisterAddressProvider()
	{
		return Stream.of(
				Arguments.of( new byte[]{ 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00 }, ModbusDataAccess.BAYMInputRegisterAddress.VOLTAGE_REGISTER_ADDRESS ) );
	}

	@ParameterizedTest
	@MethodSource( "getDataAsRegisterAddressProvider" )
	public void getDataAsRegisterAddress( byte[] input, ModbusDataAccess.BAYMInputRegisterAddress address )
	{
		assertEquals( (new ModbusDataAccess( input )).getDataAsRegisterAddress(), address );
	}

	private static Stream< Arguments > getDataAsReadLengthProvider()
	{
		return Stream.of(
				Arguments.of( new byte[]{ 0x01, 0x02, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00 }, (short) 0x0000 ),
				Arguments.of( new byte[]{ 0x01, 0x02, 0x03, 0x04, 0x10, 0x20, 0x00, 0x00 }, (short) 0x1020 ) );
	}

	@ParameterizedTest
	@MethodSource( "getDataAsReadLengthProvider" )
	public void getDataAsReadLength( byte[] input, short expected )
	{
		assertEquals( (new ModbusDataAccess( input )).getDataAsReadLength(), expected );
	}

	private static Stream< Arguments > getCRC16Provider()
	{
		return Stream.of(
				Arguments.of( new byte[]{ 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 }, new byte[]{ 0x05, 0x06 } ) );
	}

	@ParameterizedTest
	@MethodSource("getCRC16Provider")
	public void getCRC16( byte[] input, byte[] expected )
	{
		assertArrayEquals( (new ModbusDataAccess( input )).getCRC16(), expected );
	}
}

