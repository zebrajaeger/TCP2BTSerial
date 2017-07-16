# TCP2BTSerial
 
A simple App to build a Bridge between Apps in a emulated environment 
and a Bluetooth Serial Device lie HC-05

The Bridge is a simple TCP/IP Server that handles only one connection at a time.

The IP is shown in the App interface, the port is fixed at 7654.

![Overview](https://github.com/zebrajaeger/TCP2BTSerial/blob/master/doc/overview.png "Overview")

## Example

Use i.e. Putty to make a raw connection to &lt;IP&gt;:7654. When you connected, 
every char you type should be send to your Bluetooth Serial Device.