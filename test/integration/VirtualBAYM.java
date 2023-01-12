class VirtualBAYM
{
	// TODO: convert to a byte or short?
	int m_address = 0;

	public VirtualBAYM( int address )
	{
		this.m_address = address;
	}

	public int getAddress()
	{
		return this.m_address;
	}

	private byte[] floatToBytes( float d )
	{
		int i = Float.floatToIntBits( d );

		byte[] result = new byte[4];
		result[0] = (byte)((i >> 24) & 0xFF);
		result[1] = (byte)((i >> 16) & 0xFF);
		result[2] = (byte)((i >>  8) & 0xFF);
		result[3] = (byte)((i >>  0) & 0xFF);
		return result;
	}

	private byte[] getSimpleFloatResponse( byte address, ModbusDataAccess.ModbusFunction function, float f )
	{
		byte[] result = new byte[9];
		result[0] = (byte) address; 
		result[1] = function.toValue();
		result[2] = 0x04; // length of data (not include this byte) (TODO: should this be floatBytes.length)?

		// TODO: write function that appends a float to the byte array?
		byte[] floatBytes = floatToBytes( f );
		System.arraycopy( floatBytes, 0, result, 3, floatBytes.length );

		CRC16 crc = new CRC16();
		crc.update( result, result.length - 2 );

		// TODO: write generic function to CRC16 a byte array?
		byte[] crc16 = crc.getValue();
		System.arraycopy( crc16, 0, result, result.length - 2, crc16.length );
		return result;
	}

	public byte[] getVoltage( float voltage )
	{
		return getSimpleFloatResponse( 
				(byte) m_address,
				ModbusDataAccess.ModbusFunction.ReadInputRegister,
				voltage );
	}

	public byte[] getCurrent( float current )
	{
		return getSimpleFloatResponse(
				(byte) m_address,
				ModbusDataAccess.ModbusFunction.ReadInputRegister,
				current );
	}

	public byte[] getActivePower( float power )
	{
		return getSimpleFloatResponse(
				(byte) m_address,
				ModbusDataAccess.ModbusFunction.ReadInputRegister,
				power );
	}

	public byte[] getReactivePower( float power )
	{
		return getSimpleFloatResponse(
				(byte) m_address,
				ModbusDataAccess.ModbusFunction.ReadInputRegister,
				power );
	}

}

