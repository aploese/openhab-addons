# Zendure Binding

This Binding integrates [Zendure devices](https://zendure.com) developed by Zendure.

## Supported Things

Curently this binding supports SolarFlow 800, SolarFlow 800 PRO and SolarFlow 1600AC.

## Discovery

In general devices need to be active to be discovered by the binding.

The binding uses mDNS to discover the Zendure devices.
They periodically announce their presence, which is used by the binding to find them on the local network.

## Binding Configuration

```text
# Configuration for the Zendure Binding
```

## Thing Configuration

### `sample` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec. | 600     | no       | yes      |

## Channels

## Full Example

### Thing Configuration

### Item Configuration

### Sitemap Configuration

## Any custom content here!
