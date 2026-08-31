# Environmental Calculations

## Milestone 3 method v1

All stored values use SI units. User-facing temperature is Celsius and pressure is hPa; the calculation boundary converts to kelvin and pascals. Every input records source, quality and capture time. Calculations accept station/surface pressure only. Mean-sea-level pressure and altimeter settings are stored as separate provider metadata and are never substituted for station pressure.

### Water vapour and density

Saturation vapour pressure uses the Buck equation for water:

```text
es_hPa = 6.1121 * exp((18.678 - tC / 234.5) * (tC / (257.14 + tC)))
pv_Pa  = relativeHumidityFraction * es_hPa * 100
pd_Pa  = stationPressurePa - pv_Pa
rho    = pd_Pa / (287.05 * temperatureK) + pv_Pa / (461.495 * temperatureK)
```

Density ratio uses 1.225 kg/m³ as the ISA sea-level reference. Dew point uses the Magnus inverse with constants 17.625 and 243.04 °C; zero humidity reports no finite physical dew point as the implementation sentinel `0 K`.

### Pressure and density altitude

Pressure altitude uses the ISA troposphere relation:

```text
44330.769 * (1 - (stationPressurePa / 101325)^0.190263)
```

Density altitude is not a rough temperature shortcut. It is the altitude whose ISA density equals the calculated moist-air density, solved by 80-step bisection over -1,000 m to 20,000 m using `T = 288.15 - 0.0065h` and the hydrostatic pressure relation.

### Speed of sound

The current method uses `sqrt(1.4 * 287.05 * temperatureK)`. Humidity affects density but not this first method's speed-of-sound correction; this limitation remains visible for later validation.

### Pressure sampling

Barometer collection runs for seven seconds, drops the first two settling samples, rejects values outside 300–1,100 hPa, reports mean, median, minimum, maximum and sample standard deviation, and marks a sample unstable above 0.35 hPa standard deviation. The user may retry or retain a manual value.

No battery, CPU, GPU or device thermal measurement is accepted as ambient air temperature.
