:: Modify portions of this script only if you want to run custom logic handling Azure RoleEnvironment events at runtime: Changing and Stopping.
:: The first input parameter (%1) corresponds to the event name.
:: If the event is Changing, the second input parameter (%2) corresponds to the type of the change: RoleEnvironmentConfigurationSettingChange or RoleEnvironmentTopologyChange
:: Make sure your custom logic returns quickly.

:: Logging the change event history for this instance
echo %date% %time% %1 %2 >> change.log

:: Event handler for the Stopping event
if %1==Stopping (
	:: Add your custom logic here if needed. Make sure it does not take long to return.
	exit 0
)

:: Event handler for the Changing event
if %1==Changing (
	if %2==RoleEnvironmentConfigurationSettingChange (
		:: Add your custom logic here if needed to handle a configuration setting change. 
		exit 0
	) else if %2==RoleEnvironmentTopologyChange (
		:: Add your custom logic here if needed to handle a topology change (adding or removing role instances)
		exit 0
	)
)
