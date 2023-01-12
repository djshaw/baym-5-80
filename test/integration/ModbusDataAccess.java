import java.util.Arrays;

class ModbusDataAccess
{
	private byte[] m_data;

	public ModbusDataAccess( byte[] data )
	{
		m_data = data;
		// TODO: validate the CRC16 value
	}

	public enum ModbusFunction
	{
		// TODO: ALL_CAPS
		ReadCoilStatus( 1 ),
		ReadInputStatus( 2 ),
		ReadHoldingRegister( 3 ),
		ReadInputRegister( 4 ),
		WriteSingleCoil( 5 ),
		WriteSingleRegister( 6 ),
		WriteMultipleCoils( 7 ),
		WriteMultipleRegisters( 8 ),

		UnknownFunction( 0 );

		private final byte m_value;
		ModbusFunction( int value )
		{
			this.m_value = (byte) value;
		}

		// TODO: rename to getModbusFunctionCode() (or similar)?
		public byte toValue()
		{
			return this.m_value;
		}
	}

	public static enum BAYMInputRegisterAddress
	{
		VOLTAGE_REGISTER_ADDRESS     		 ( (short) 0x0000 ),
		CURRENT_REGISTER_ADDRESS     		 ( (short) 0x0008 ),
		ACTIVE_POWER_REGISTER_ADDRESS        ( (short) 0x0012 ),
		REACTIVE_POWER_REGISTER_ADDRESS		 ( (short) 0x001A ),
		// TODO: more
		TOTAL_REACTIVE_POWER_REGISTER_ADDRESS( (short) 0x0400 ),

		UNKNOWN_REGISTER_ADDRESS             ( (short) 0xFFFF );

		private short m_address;

		BAYMInputRegisterAddress( short b )
		{
			this.m_address = b;
		}

		public short getAddress()
		{
			return this.m_address;
		}
	}

	private ModbusFunction byteToModbusFunctionEnum( byte b )
	{
		switch( b )
		{
			case 0x01: return ModbusFunction.ReadCoilStatus;
			case 0x02: return ModbusFunction.ReadInputStatus;
			case 0x03: return ModbusFunction.ReadHoldingRegister;
			case 0x04: return ModbusFunction.ReadInputRegister;
			case 0x05: return ModbusFunction.WriteSingleCoil;
			case 0x06: return ModbusFunction.WriteSingleRegister;
			case 0x07: return ModbusFunction.WriteMultipleCoils;
			case 0x08: return ModbusFunction.WriteMultipleRegisters;
		}

		return ModbusFunction.UnknownFunction;
	}

	public static BAYMInputRegisterAddress shortToInputRegisterEnum( short b )
	{
		switch( b )
		{
			case 0x0000: return BAYMInputRegisterAddress.VOLTAGE_REGISTER_ADDRESS;
			case 0x0008: return BAYMInputRegisterAddress.CURRENT_REGISTER_ADDRESS;
			case 0x0012: return BAYMInputRegisterAddress.ACTIVE_POWER_REGISTER_ADDRESS;
			case 0x001A: return BAYMInputRegisterAddress.REACTIVE_POWER_REGISTER_ADDRESS;
			// TODO: more
			case 0x0400: return BAYMInputRegisterAddress.TOTAL_REACTIVE_POWER_REGISTER_ADDRESS;
		}

		return BAYMInputRegisterAddress.UNKNOWN_REGISTER_ADDRESS;
	}

	public static BAYMInputRegisterAddress bytesToRegisterEnum( byte[] b )
	{
		// TODO error if b == null || bytes.length != 2
		return shortToInputRegisterEnum( (short)(((b[0] << 8) & 0xFF00) | ((b[1] << 0) & 0x00FF) ));
	}

	public byte getAddress()
	{
		// TODO: how to handle malformed packets? (i.e. packet too short, or null m_data)
		return m_data[0];
	}

	public ModbusFunction getFunction()
	{
		// TODO: how to handle malformed packets? (i.e. empty packet, or null m_data)
		return byteToModbusFunctionEnum( m_data[1] );
	}

	// TODO: is m_data[2] always the length of the data? Or is it a baym specifc thing?

	public byte[] getData()
	{
		// TODO: how to handle malformed packets? (i.e. packet too short, or null m_data)
		return Arrays.copyOfRange( m_data, 2, 				  m_data.length - 2 );
	}

	// WARNING: This is not a generic getDataAsDouble for converting general
	// data into a float. In particular, the libmodbus library implies that the
	// double can be incoded in different orders.
	public float getDataAsFloat()
	{
		byte[] data = getData();
		// TODO: assert data.length == 5 && data[0] == 4?
		// Java likes to sign extend stuff. Be careful, the bit masking is important.
		int intBits = ((data[1] << 24) & 0xFF000000)
					| ((data[2] << 16) & 0x00FF0000)
					| ((data[3] << 8 ) & 0x0000FF00)
					| ((data[4] << 0 ) & 0x000000FF);
		// TODO: how to handle malformed packets?
		// TODO: verify/assert data is 4 bytes
		return Float.intBitsToFloat( intBits );
	}

	public BAYMInputRegisterAddress getDataAsRegisterAddress()
	{
		byte[] data = getData();
		// TODO: verify that data.length >= 2
        return bytesToRegisterEnum( Arrays.copyOfRange( data, 0, 2 ) );
	}

	public short getDataAsReadLength()
	{
		byte[] data = getData();
		// TODO: verify that data.length >= 4
		// TODO: are there sign extension issues to account for?
		return (short)(((data[2] << 8) & 0xFF00 )
		             | ((data[3] << 0) & 0x00FF ));
	}

	public byte[] getCRC16()
	{
		// TODO: how to handle malformed packets? (i.e. packet too short, or null m_data, or mismatched crc)
		return Arrays.copyOfRange( m_data, m_data.length - 2, m_data.length );
	}
}

