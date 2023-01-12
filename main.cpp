#include <prometheus/exposer.h>
#include <prometheus/gauge.h>
#include <prometheus/registry.h>

#include <cstring>
#include <iostream>
#include <memory>

#include <termios.h>

#include <modbus.h>

void usage()
{
    // clang-format off
    std::cout << "Usage:" << std::endl
              << "\tpowermonitoring [-g] $RS_485_DEV_TTY" << std::endl;
    // clang-format on
}

namespace std
{

template < typename EF > struct scope_exit
{
private:
    scope_exit( scope_exit const & )                = delete;
    void         operator=( scope_exit const & )    = delete;
    scope_exit & operator=( scope_exit && )         = delete;
    EF           exit_function;
    bool         execute_on_destruction;
    // exposition only

public:
    // construction
    explicit scope_exit( EF && f ) noexcept : exit_function( std::move( f ) ), execute_on_destruction{ true } 
    {
        // Nothing
    }

    // move
    scope_exit( scope_exit && rhs ) noexcept : exit_function( std::move( rhs.exit_function ) ),
                                               execute_on_destruction{ rhs.execute_on_destruction }
    {
        rhs.release();
    }

    // release
    ~scope_exit() noexcept
    {
        if( execute_on_destruction )
        {
            this->exit_function();
        }
    }

    void release() noexcept { this->execute_on_destruction = false; }
};

template < typename EF > scope_exit< EF > make_scope_exit( EF && exit_function ) noexcept
{
    return scope_exit< EF >( std::forward< EF >( exit_function ) );
}

}

class ModbusDataAccess
{
    modbus_t * m_ctx;

public:
    // TODO: a more DataAccess oriented initialization (static function that
    // returns a ModbusDataAccess?)
    ModbusDataAccess( modbus_t * ctx )
      : m_ctx( ctx )
    {
        // Nothing
    }

    // TODO: Using int because libmodbus is
    virtual int setSlave( int slave )
    {
        return modbus_set_slave( m_ctx, slave );
    }

    virtual int readInputRegisters( int addr, int responseSize, uint16_t * response ) const
    {
        return modbus_read_input_registers( m_ctx, addr, responseSize, response );
    };
};

class BAYMDataAccess
{
    // TODO: rename suffix to INPUT_REGISTER_ADDRESS?
    static const int VOLTAGE_REGISTER_ADDRESS              = 0x0000;
    static const int CURRENT_REGISTER_ADDRESS              = 0x0008;
    static const int ACTIVE_POWER_REGISTER_ADDRESS         = 0x0012;
    static const int REACTIVE_POWER_REGISTER_ADDRESS       = 0x001A;
    static const int POWER_FACTOR_REGISTER_ADDRESS         = 0x002A;
    static const int FREQUENCY_REGISTER_ADDRESS            = 0x0036;
    static const int TOTAL_ACTIVE_POWER_REGISTER_ADDRESS   = 0x0100;
    static const int TOTAL_REACTIVE_POWER_REGISTER_ADDRESS = 0x0400;

    int                 m_address;
    ModbusDataAccess *  m_modbusDataAccess;

    // TODO: is float a better datatype?
    // TODO: rename to readInputFloat()
    int readFloat( int registerAddress, double * value ) const
    {
        int result = m_modbusDataAccess->setSlave( m_address );
        if( result != 0 )
        {
            return result;
        }

        size_t responseSize = 2;
        auto response = std::make_unique< uint16_t[] >( responseSize );

        result = m_modbusDataAccess->readInputRegisters( registerAddress, responseSize, response.get() );
        if( result != 2 )
        {
            return result;
        }

        *value = modbus_get_float_abcd( response.get() );
        return 0;
    }

public:
    enum
    {
        BAYM_EVEN,
        BAYM_ODD,
        BAYM_NONE,
    };

    BAYMDataAccess( ModbusDataAccess * modbusDataAccess, int address )
      : m_address( address ),
        m_modbusDataAccess( modbusDataAccess )
    {
        // Nothing
    }

    virtual ~BAYMDataAccess()
    {
        // Nothing
    }

    virtual double getVoltage() const
    {
        double result = 0.0;
        readFloat( VOLTAGE_REGISTER_ADDRESS, &result );
        // TODO: check return value
        return result;
    }

    double getCurrent() const
    {
        // modbus_read_registers() reads static data. Presumably, these are the
        // fields labeled "parameter register list" in the manual. The parameters
        // are baud rate, check digit, address, and relay control. All parameters
        // appear to be integer values.
        //
        // modbus_read_input_registers() reads data collected by the meter.
        //
        // It's really odd that the slave id is installed in the context. They 
        // should be reasonably separated since you can connect to multiple slaves
        // on the same serial connection.

        double result = 0.0;
        readFloat( CURRENT_REGISTER_ADDRESS, &result );
        // TODO: check return value
        return result;
    }

    double getActivePower() const
    {
        double result = 0.0;
        readFloat( ACTIVE_POWER_REGISTER_ADDRESS, &result );
        return result;
    }

    double getReactivePower() const
    {
        double result = 0.0;
        readFloat( REACTIVE_POWER_REGISTER_ADDRESS, &result );
        return result;
    }

    double getPowerFactor() const
    {
        double result = 0.0;
        readFloat( POWER_FACTOR_REGISTER_ADDRESS, &result );
        return result;
    }

    double getFrequency() const
    {
        double result = 0.0;
        readFloat( FREQUENCY_REGISTER_ADDRESS, &result );
        return result;
    }

    double getTotalActivePower() const
    {
        double result = 0.0;
        readFloat( TOTAL_ACTIVE_POWER_REGISTER_ADDRESS, &result );
        return result;
    }

    double getTotalReactivePower() const
    {
        double result = 0.0;
        readFloat( TOTAL_REACTIVE_POWER_REGISTER_ADDRESS, &result );
        return result;
    }

    speed_t getBaudRate() const
    {
        // TODO: implement
        return B9600;
    }

    int getCheckDigit() const
    {
        // TODO: implement
        return BAYM_EVEN;
    }

    int getAddress() const
    {
        // TODO: implement
        return 0;
    }

    bool getRelayState() const
    {
        // TODO: implement
        return true;
    }
};

int main( int argc, char * argv[] )
{
    // TODO: better parameter parsing! Just about anything will be better than
    // the bespoke logic here!
    if( argc < 2
     || ( argc == 3 && strncmp( argv[1], "-g", 3 ) != 0 )
     || argc > 3 )
    {
        usage();
        return 1;
    }

    // TODO: rename to ttyDevice to not confuse it with the meter devices
    std::string device;
    if( argc == 2 ) 
    {
        device = argv[1];
    }
    if( argc == 3 )
    {
        device = argv[2];
    }
    if( device.empty() )
    {
        usage();
        return 1;
    }

    bool debug = argc == 3 && strncmp( argv[1], "-g", 3 ) == 0;

    // TODO: use speed_t type? B9600?
    modbus_t * ctx = modbus_new_rtu( device.c_str(), 9600, 'E', 8, 1 );

    // TODO: make the baym device addresses commandline parameters
    int baymAddress = 15;

    if( ctx == nullptr )
    {
        std::cerr << "Unable to allocate the libmodbus context" << std::endl;
        return -1;
    }

    auto free_ctx = std::make_scope_exit( 
            [ ctx ]
            {
                if( ctx != nullptr ) 
                { 
                    modbus_free( ctx ); 
                } 
            } );
    auto close_ctx = std::make_scope_exit(
            [ ctx ]
            {
                if( ctx != nullptr )
                {
                    modbus_close( ctx );
                }
            } );

    std::unique_ptr< prometheus::Exposer > exposer;
    try
    {
        exposer = std::make_unique< prometheus::Exposer>( "0.0.0.0:8080" );
    }
    catch( const std::runtime_error & e )
    {
        std::cerr << "Unable to start the prometheus exposer" << std::endl;
    }

    auto registry = std::make_shared< prometheus::Registry >();

    // TODO: parameterize on the address of the power monitor
    // TODO: support multip baym devices
    auto & voltageGaugeFamily =
        prometheus::BuildGauge()
            .Name( "voltage" )
            .Help( "Voltage across the meter" )
            .Register( *registry );
    // TODO: factor out the {{ "address", ... }}
    auto & voltageGauge = voltageGaugeFamily.Add( {{ "address", std::to_string( baymAddress ) }} );

    auto & ampGaugeFamily = 
        prometheus::BuildGauge()
            .Name( "current" )
            .Help( "A" )
            .Register( *registry );
    auto & ampGauge = ampGaugeFamily.Add( {{ "address", std::to_string( baymAddress ) }} );

    auto & activePowerFamily =
        prometheus::BuildGauge()
            .Name( "power" )
            .Help( "Kwh" )
            .Register( *registry );
    auto & activePowerGauge = activePowerFamily.Add( {{ "address", std::to_string( baymAddress ) }} );

    auto & reactivePowerFamily =
        prometheus::BuildGauge()
            .Name( "reactivePower" )
            .Help( "Var" )
            .Register( *registry );
    auto & reactivePowerGauge = reactivePowerFamily.Add( {{ "address", std::to_string( baymAddress ) }} );

    auto & powerFactorFamily =
        prometheus::BuildGauge()
            .Name( "powerFactor" )
            .Help( "Cos Thetha" )
            .Register( *registry );
    auto & powerFactorGauge = powerFactorFamily.Add( {{ "address", std::to_string( baymAddress ) }} );

    auto & frequencyFamily =
        prometheus::BuildGauge()
            .Name( "frequency" )
            .Help( "Hz" )
            .Register( *registry );
    auto & frequencyGauge = frequencyFamily.Add( {{ "address", std::to_string( baymAddress ) }} );

    // TODO: I think this is monotonically increasing. It doesn't need to be a gauge.
    auto & totalActivePowerFamily =
        prometheus::BuildGauge()
            .Name( "totalActivePower" )
            .Help( "Kwh" )
            .Register( *registry );
    auto & totalActivePowerGauge = totalActivePowerFamily.Add( {{ "address", std::to_string( baymAddress ) }} );

    // TODO: I think this is monotonically increasing. It doesn't need to be a gauge.
    auto & totalReactivePowerFamily =
        prometheus::BuildGauge()
            .Name( "totalReactivePower" )
            .Help( "Kvarh" )
            .Register( *registry );
    auto & totalReactivePowerGauge = totalReactivePowerFamily.Add( {{ "address", std::to_string( baymAddress ) }} );

    exposer->RegisterCollectable( registry );

    std::cout << "Hosting metrics on http://$HOSTNAME:8080/metrics" << std::endl;

    if( modbus_enable_quirks( ctx, MODBUS_QUIRK_MAX_SLAVE ) )
    {
        std::cerr << "Unable to set quirks: " << modbus_strerror( errno ) << std::endl;
        return -1;
    }

    if( debug && modbus_set_debug( ctx, 1 ) != 0 )
    {
        std::cerr << "Unable to set debug: " << modbus_strerror( errno ) << std::endl;
        return -1;
    }

    // Set a higher response timeout because the emulated BAYM device (in java)
    // is slower to respond than the default timeout
    // TODO: make this configurable
    if( modbus_set_response_timeout( ctx, 1, 0 ) )
    {
        std::cerr << "Unable to set response timeout" << std::endl;
        return -1;
    }

    if( modbus_connect( ctx ) != 0 )
    {
        std::cerr << "Unable to connect: " << modbus_strerror( errno ) << std::endl;
        return -1;
    }

    // TODO: Support multiple baym devices
    auto modbusDataAccess = std::make_unique< ModbusDataAccess >( ctx );
    auto baymDataAccess   = std::make_unique< BAYMDataAccess >( modbusDataAccess.get(), baymAddress );
    for( ;; )
    {
        // TODO: investigate what happens when a long read length is requested.
        // Can we dump the entire memory space with one request, then pluck
        // data out of the response as necessary?
        voltageGauge.Set( baymDataAccess->getVoltage() );
        ampGauge.Set( baymDataAccess->getCurrent() );
        activePowerGauge.Set( baymDataAccess->getActivePower() );
        reactivePowerGauge.Set( baymDataAccess->getReactivePower() );
        powerFactorGauge.Set( baymDataAccess->getPowerFactor() );
        frequencyGauge.Set( baymDataAccess->getFrequency() );
        totalActivePowerGauge.Set( baymDataAccess->getTotalActivePower() );
        totalReactivePowerGauge.Set( baymDataAccess->getTotalReactivePower() );
    }

    // scope exit calls modbus_close();
    // scope exit calls modbus_free()

    return 0;
}
