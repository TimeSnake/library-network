host_name = "${server.getChannelHostName()}"
listen_host_name = "${server.getChannelListenHostName()}"
port_offset = ${server.getChannelPortOffset()}

[proxy]
server_name = "${server.getChannelProxyServerName()}"
host_name = "${server.getChannelProxyHostName()}"
port = ${server.getChannelProxyPort()}