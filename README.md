# BurpMCP

![BurpMCP Logo](assets/cover_image.png)

BurpMCP is a Burp Suite extension that augments application security testers, vulnerability researchers, and bug bounty hunters with modern AI. Every day, large language models gain larger context windows, faster response times, and improved knowledge and reasoning skills. BurpMCP lets you take advantage of these capabilities while testing HTTP-based applications, providing a super-intelligent sidekick to help navigate unfamiliar attack surfaces and chase down complex vulnerabilities.

BurpMCP focuses on enhancing manual application security testing with the help of LLMs by integrating Burp Suite with Model Context Protocol (MCP) clients like Claude Desktop, Cursor, etc. so you can prompt AI to perform autonomous testing on your behalf with full control and visibility of the model's actions.

Features:

- :notebook: Save requests in the extension for MCP clients to retrieve using the Get-Saved-Request Tool 
- :hammer: Send new HTTP/1.1 and HTTP/2 requests and view them in the Request Logs tab
- :pencil2: Resend saved requests with regex string replacements for faster tweaking (like Repeater for LLMs)
- :satellite: Generate Collaborator payloads and retrieve interactions for LLM-led out-of-band testing
- :microscope: View all MCP messages in the Server logs tab for easy debugging

The extension starts an MCP Server that interfaces directly with the Burp Suite extension API. For more information on Model Context Protocol, refer to the [docs](https://modelcontextprotocol.io/introduction).

## Installation

Download the jar file from the releases and load it into Burp to install.

The MCP server runs by default on localhost port 8181 over SSE. The configuration syntax varies depending on your MCP client, but here are a few examples:

[Cline](https://cline.bot/):
```json
{
  "mcpServers": {
    "burpmcp": {
      "autoApprove": [],
      "disabled": false,
      "timeout": 30,
      "url": "http://localhost:8181/mcp/sse",
      "transportType": "sse"
    }
  }
}
```

[Dive](https://github.com/OpenAgentPlatform/Dive):
```json
{
  "mcpServers": {
    "BurpMCP": {
      "transport": "sse",
      "url": "http://localhost:8181/mcp/sse"
    }
  }
}
```

To use BurpMCP with STDIO-only clients like **Claude Desktop**, download the `stdio-bridge.py` script and install the required dependencies:

```sh
pip3 install typer mcp
```

Then, add the following configuration to your `claude_desktop_config.json`. Make sure the host and port match what you configure in BurpMCP.

```json
{
  "mcpServers": {
    "BurpMCP": {
      "command": "python3",
      "args": ["path/to/stdio-bridge.py", "http://localhost:8181/mcp/sse"],
      "env": {}
    }
  }
}
```

## Usage

To send requests to BurpMCP, right-click any request and click `Extensions -> Send to BurpMCP".

![Send a request to BurpMCP](assets/send_to_burpmcp.png)

The request will then be visible in the "Saved Requests" tab in BurpMCP. Each saved request includes a Notes column to provide LLMs with additional context. Clients can also update the notes in each saved request to keep track of important details or save requests for future analysis.

![Saved requests tab](assets/saved_requests.png)

Using your preferred MCP Client, you can prompt the model to retrieve saved requests and send follow-up requests to assist with testing.

![Retrieve the saved request](assets/retrieve_saved_request.png)

Claude attempting to solve the lab:

![Claude trying to solve the lab](assets/autonomous_testing.png)

## Examples

Check out the [Showcase](Showcase) for examples of chat logs where BurpMCP is successfully used.

Thank you to the following for providing examples:

- [hunterverhelst](https://github.com/hunterverhelst)

## Common Issues

- The models sometimes forget to add important components to requests like Content-Length headers or URL encoding. This is not an issue with the extension but rather a failure on the model's part.
- The HTTP/2 tool parameter specifies that forbidden headers should not be included in HTTP/2 requests, but the models may sometimes ignore it. If an HTTP/2 request fails, check the request for any invalid headers.
- Some MCP clients seem to be unable to send CRLFs ("\r\n"). CRLF line endings are required for compliant HTTP/1.1 requests. To remedy this, automatic LF to CRLF replacement can be enabled.
- Some MCP clients will not acknowledge an error response after an extended period of time and go on generating forever. Adding a custom timeout on the server side does not fix this issue. At the moment, the best solution is to use a client that supports timeouts, such as [Cline](https://cline.bot/).

## Limitations

- When LF to CRLF replacement is enabled, testing for HTTP/1.1 request smuggling will be challenging since the Content-Length header is automatically updated to reflect the additional characters.
- When resending requests over HTTP/2, the headers are joined by newlines and re-split before the request is sent. Also, cookies are split into their own headers. This makes any sort of HTTP/2 protocol vulnerability testing like request smuggling difficult if newlines need to be manually injected into header values. 

## Tool Definitions

The tool specifications can be found in [src/main/java/burpmcp/tools](src/main/java/burpmcp/tools) and describe exactly what data the MCP server expects from the client. This information is important to understand so you know what the models are sending. Depending on your use case, you can modify the property descriptions, which would require rebuilding the extension.

## Building from Source

```bash
git clone https://github.com/swgee/burpmcp.git
cd burpmcp
mvn clean package
```

## Contributing

Please create an issue for any bugs, feature requests, or questions. If you would like to contribute, submit a PR. If you have used the tool to find vulnerabilities or anything else interesting, please send screenshots or chat logs, and I would be glad to include them in the Showcase. Thank you for using BurpMCP, and happy hacking!
