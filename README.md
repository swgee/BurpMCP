# BurpMCP

### What is this?

A Burp Suite extension that exposes a Model Context Protocol (MCP) server for LLMs to interact with Burp Suite.

### Installation

1. Install the extension in Burp Suite
2. If using Claude Desktop, add the following to `claude_desktop_config.json`:

___TODO___

### Usage

1. Right click on requests in Burp Suite, select "Extensions" -> "BurpMCP" -> "Send to BurpMCP"
2. The request content will be available to MCP clients as a resource. View all resources in the BurpMCP tab.
3. You can now prompt your LLM to interact with the resource requests. 

The MCP Server supports:
- Viewing requests sent to the extension
- Modifying requests and sending them. Requests sent by MCP clients can be seen in the Logger tab.