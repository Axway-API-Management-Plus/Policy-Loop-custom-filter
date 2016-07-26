# Description
This filter allows you to loop on a Policy. There is actually 4 exit conditions:
- The looping Policy returns 'false',
- The loop condition expression returns 'false',
- The maximum count of iteration has been reached,
- The maximum loop iteration delay has expired.

## API Management Version Compatibilty
This artefact was successfully tested for the following versions:
- V7.4.0
- V7.4.1


## Install

```
• Copy the jar file in the API Gateway VORDEL_HOME/ext/lib and restart API Gateway instances
• Add the jar file to Policy Studio Runtime Dependencies,
• Import the custom filter set file in the Policy studio.

```

## Usage

```
You can choose between while or do/while loop.
Each of exit condition can be handled as success (filter returns true) or error (filter returns false).
The loop condition is a selector expression (like the evaluate filter).
Be aware the maximum loop iteration time is checked AFTER the child policy has returned, it WILL NOT stop the child process.
```
![alt text][Screenshot1]

[Screenshot1]: https://github.com/Axway/api-management-artefacts/blob/master/Policy-Loop-custom-filter/Policy%20Loop%20custom%20filter.png  "Policy Loop Filter Configuration"


## Bug and Caveats
```
Selector expressions used in configuration do not appear as requested attributes in policy studio (to be done)
```

## Changelog
- Added exit condition error configuration (correct the 'no iteration bug returning false').
- Corrected recursive policy configuration.


## Contributing

Please read [Contributing.md] (https://github.com/Axway/api-management-artefacts/blob/master/Contributing.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Team

![alt text][Axwaylogo] Axway Team

[Axwaylogo]: https://github.com/Axway/api-management-artefacts/blob/master/AxwayLogoSmall.png  "Axway logo"


## License
Apache License 2.0 (refer to document [license] (https://github.com/Axway-API-Management/Policy-Loop-custom-filter/blob/master/LICENSE))
