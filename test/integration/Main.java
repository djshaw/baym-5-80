public class Main
{
	public static void main( String[] args )
		throws Exception
	{
		int baymAddress = 15;
		VirtualBAYM baym = new VirtualBAYM( baymAddress );

		byte[] bytes = new byte[256];
		while( true )
		{
			int read = System.in.read( bytes );
			ModbusDataAccess mda = new ModbusDataAccess( bytes );

			byte[] output = null;

			if( mda.getAddress() == baym.getAddress() )
			{
				switch( mda.getFunction() )
				{
					case ReadHoldingRegister:
						// TODO: return BAYM configuration values
						break;

					case ReadInputRegister:
						switch( mda.getDataAsRegisterAddress() )
						{
							case VOLTAGE_REGISTER_ADDRESS:
								// TODO: verify that the data read length == 2
								// TODO: what happens if we read past the address that contains the
								// voltage? Will it include part/all of the current register?
								// TODO: add noise to the data so the graphs show something slightly
								// more interesting.
								output = baym.getVoltage( 120.0f );
								break;

							case CURRENT_REGISTER_ADDRESS:
								output = baym.getCurrent( 1.0f );
								break;

							case ACTIVE_POWER_REGISTER_ADDRESS:
								output = baym.getActivePower( 2.0f );
								break;

							case REACTIVE_POWER_REGISTER_ADDRESS:
								output = baym.getReactivePower( 3.0f );
								break;

							default:
								// TODO: log error? (the client will timeout)
								break;
						}
						break;

					default:
						// TODO: log error? (the client will timeout)
						break;
				}
			}

			if( output != null )
			{
				System.out.write( output );
			}
		}
	}
}

